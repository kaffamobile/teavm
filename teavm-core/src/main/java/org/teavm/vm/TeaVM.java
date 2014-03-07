/*
 *  Copyright 2013 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.vm;

import java.io.*;
import java.util.*;
import org.teavm.codegen.*;
import org.teavm.common.FiniteExecutor;
import org.teavm.dependency.*;
import org.teavm.javascript.*;
import org.teavm.javascript.ast.ClassNode;
import org.teavm.javascript.ni.Generator;
import org.teavm.model.*;
import org.teavm.model.util.ListingBuilder;
import org.teavm.model.util.ProgramUtils;
import org.teavm.model.util.RegisterAllocator;
import org.teavm.optimization.ClassSetOptimizer;
import org.teavm.optimization.Devirtualization;
import org.teavm.vm.spi.RendererListener;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;

/**
 *
 * @author Alexey Andreev
 */
public class TeaVM implements TeaVMHost {
    private JavascriptProcessedClassSource classSource;
    private DependencyChecker dependencyChecker;
    private FiniteExecutor executor;
    private ClassLoader classLoader;
    private boolean minifying = true;
    private boolean bytecodeLogging;
    private OutputStream logStream = System.out;
    private Map<String, TeaVMEntryPoint> entryPoints = new HashMap<>();
    private Map<String, String> exportedClasses = new HashMap<>();
    private Map<MethodReference, Generator> methodGenerators = new HashMap<>();
    private List<RendererListener> rendererListeners = new ArrayList<>();
    private Properties properties = new Properties();

    TeaVM(ClassHolderSource classSource, ClassLoader classLoader, FiniteExecutor executor) {
        this.classSource = new JavascriptProcessedClassSource(classSource);
        this.classLoader = classLoader;
        dependencyChecker = new DependencyChecker(this.classSource, classLoader, executor);
        this.executor = executor;
    }

    @Override
    public void add(DependencyListener listener) {
        dependencyChecker.addDependencyListener(listener);
    }

    @Override
    public void add(ClassHolderTransformer transformer) {
        classSource.addTransformer(transformer);
    }


    @Override
    public void add(MethodReference methodRef, Generator generator) {
        methodGenerators.put(methodRef, generator);
    }

    @Override
    public void add(RendererListener listener) {
        rendererListeners.add(listener);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public boolean isMinifying() {
        return minifying;
    }

    public void setMinifying(boolean minifying) {
        this.minifying = minifying;
    }

    public boolean isBytecodeLogging() {
        return bytecodeLogging;
    }

    public void setBytecodeLogging(boolean bytecodeLogging) {
        this.bytecodeLogging = bytecodeLogging;
    }

    public void setProperties(Properties properties) {
        this.properties.clear();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    @Override
    public Properties getProperties() {
        return new Properties(properties);
    }

    public TeaVMEntryPoint entryPoint(String name, MethodReference ref) {
        if (entryPoints.containsKey(name)) {
            throw new IllegalArgumentException("Entry point with public name `" + name + "' already defined " +
                    "for method " + ref);
        }
        TeaVMEntryPoint entryPoint = new TeaVMEntryPoint(name, ref,
                dependencyChecker.linkMethod(ref, DependencyStack.ROOT));
        dependencyChecker.initClass(ref.getClassName(), DependencyStack.ROOT);
        entryPoints.put(name, entryPoint);
        return entryPoint;
    }

    public TeaVMEntryPoint linkMethod(MethodReference ref) {
        TeaVMEntryPoint entryPoint = new TeaVMEntryPoint("", ref,
                dependencyChecker.linkMethod(ref, DependencyStack.ROOT));
        dependencyChecker.initClass(ref.getClassName(), DependencyStack.ROOT);
        return entryPoint;
    }

    public void exportType(String name, String className) {
        if (exportedClasses.containsKey(name)) {
            throw new IllegalArgumentException("Class with public name `" + name + "' already defined for class " +
                    className);
        }
        dependencyChecker.initClass(className, DependencyStack.ROOT);
        exportedClasses.put(name, className);
    }

    public void linkType(String className) {
        dependencyChecker.initClass(className, DependencyStack.ROOT);
    }

    public ClassHolderSource getClassSource() {
        return classSource;
    }

    public void prepare() {
        dependencyChecker.startListeners();
    }

    public boolean hasMissingItems() {
        return dependencyChecker.hasMissingItems();
    }

    public void showMissingItems(Appendable target) throws IOException {
        dependencyChecker.showMissingItems(target);
    }

    public void checkForMissingItems() {
        dependencyChecker.checkForMissingItems();
    }

    public void build(Appendable writer, BuildTarget target) throws RenderingException {
        AliasProvider aliasProvider = minifying ? new MinifyingAliasProvider() : new DefaultAliasProvider();
        DefaultNamingStrategy naming = new DefaultNamingStrategy(aliasProvider, classSource);
        naming.setMinifying(minifying);
        SourceWriterBuilder builder = new SourceWriterBuilder(naming);
        builder.setMinified(minifying);
        SourceWriter sourceWriter = builder.build(writer);
        dependencyChecker.linkMethod(new MethodReference("java.lang.Class", "createNew",
                ValueType.object("java.lang.Class")), DependencyStack.ROOT).use();
        dependencyChecker.linkMethod(new MethodReference("java.lang.String", "<init>",
                ValueType.arrayOf(ValueType.CHARACTER), ValueType.VOID), DependencyStack.ROOT).use();
        dependencyChecker.linkMethod(new MethodReference("java.lang.String", "getChars",
                ValueType.INTEGER, ValueType.INTEGER, ValueType.arrayOf(ValueType.CHARACTER), ValueType.INTEGER,
                ValueType.VOID), DependencyStack.ROOT).use();
        dependencyChecker.linkMethod(new MethodReference("java.lang.String", "length", ValueType.INTEGER),
                DependencyStack.ROOT).use();
        dependencyChecker.linkMethod(new MethodReference("java.lang.Object", new MethodDescriptor("clone",
                ValueType.object("java.lang.Object"))), DependencyStack.ROOT).use();
        executor.complete();
        if (hasMissingItems()) {
            return;
        }
        Linker linker = new Linker(dependencyChecker);
        ListableClassHolderSource classSet = linker.link(classSource);
        Decompiler decompiler = new Decompiler(classSet, classLoader, executor);
        devirtualize(classSet, dependencyChecker);
        executor.complete();
        ClassSetOptimizer optimizer = new ClassSetOptimizer(executor);
        optimizer.optimizeAll(classSet);
        executor.complete();
        allocateRegisters(classSet);
        executor.complete();
        if (bytecodeLogging) {
            try {
                logBytecode(new PrintWriter(new OutputStreamWriter(logStream, "UTF-8")), classSet);
            } catch (UnsupportedEncodingException e) {
                // Just don't do anything
            }
        }
        for (Map.Entry<MethodReference, Generator> entry : methodGenerators.entrySet()) {
            decompiler.addGenerator(entry.getKey(), entry.getValue());
        }
        List<ClassNode> clsNodes = decompiler.decompile(classSet.getClassNames());
        Renderer renderer = new Renderer(sourceWriter, classSet, classLoader);
        try {
            for (RendererListener listener : rendererListeners) {
                listener.begin(renderer, target);
            }
            renderer.renderRuntime();
            for (ClassNode clsNode : clsNodes) {
                ClassReader cls = classSet.get(clsNode.getName());
                for (RendererListener listener : rendererListeners) {
                    listener.beforeClass(cls);
                }
                renderer.render(clsNode);
                for (RendererListener listener : rendererListeners) {
                    listener.afterClass(cls);
                }
            }
            for (Map.Entry<String, TeaVMEntryPoint> entry : entryPoints.entrySet()) {
                sourceWriter.append(entry.getKey()).ws().append("=").ws().appendMethodBody(entry.getValue().reference)
                        .append(";").softNewLine();
            }
            for (Map.Entry<String, String> entry : exportedClasses.entrySet()) {
                sourceWriter.append(entry.getKey()).ws().append("=").ws().appendClass(entry.getValue()).append(";")
                        .softNewLine();
            }
            for (RendererListener listener : rendererListeners) {
                listener.complete();
            }
        } catch (IOException e) {
            throw new RenderingException("IO Error occured", e);
        }
    }

    // TODO: repair devirtualization
    private void devirtualize(ListableClassHolderSource classes, DependencyInfo dependency) {
        final Devirtualization devirtualization = new Devirtualization(dependency, classes);
        for (String className : classes.getClassNames()) {
            ClassHolder cls = classes.get(className);
            for (final MethodHolder method : cls.getMethods()) {
                if (method.getProgram() != null) {
                    executor.execute(new Runnable() {
                        @Override public void run() {
                            devirtualization.apply(method);
                        }
                    });
                }
            }
        }
    }

    private void allocateRegisters(ListableClassHolderSource classes) {
        for (String className : classes.getClassNames()) {
            ClassHolder cls = classes.get(className);
            for (final MethodHolder method : cls.getMethods()) {
                if (method.getProgram() != null && method.getProgram().basicBlockCount() > 0) {
                    executor.execute(new Runnable() {
                        @Override public void run() {
                            RegisterAllocator allocator = new RegisterAllocator();
                            Program program = ProgramUtils.copy(method.getProgram());
                            allocator.allocateRegisters(method, program);
                            method.setProgram(program);
                        }
                    });
                }
            }
        }
    }

    private void logBytecode(PrintWriter writer, ListableClassHolderSource classes) {
        for (String className : classes.getClassNames()) {
            ClassHolder classHolder = classes.get(className);
            printModifiers(writer, classHolder);
            writer.println("class " + className);
            for (MethodHolder method : classHolder.getMethods()) {
                logMethodBytecode(writer, method);
            }
        }
    }

    private void logMethodBytecode(PrintWriter writer, MethodHolder method) {
        writer.print("    ");
        printModifiers(writer, method);
        writer.print(method.getName() + "(");
        ValueType[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (i > 0) {
                writer.print(", ");
            }
            printType(writer, parameterTypes[i]);
        }
        writer.println(")");
        Program program = method.getProgram();
        if (program != null && program.basicBlockCount() > 0) {
            ListingBuilder builder = new ListingBuilder();
            writer.print(builder.buildListing(program, "        "));
            writer.print("        Register allocation:");
            for (int i = 0; i < program.variableCount(); ++i) {
                writer.print(i + ":" + program.variableAt(i).getRegister() + " ");
            }
            writer.println();
            writer.println();
            writer.flush();
        } else {
            writer.println();
        }
    }

    private void printType(PrintWriter writer, ValueType type) {
        if (type instanceof ValueType.Object) {
            writer.print(((ValueType.Object)type).getClassName());
        } else if (type instanceof ValueType.Array) {
            printType(writer, ((ValueType.Array)type).getItemType());
            writer.print("[]");
        } else if (type instanceof ValueType.Primitive) {
            switch (((ValueType.Primitive)type).getKind()) {
                case BOOLEAN:
                    writer.print("boolean");
                    break;
                case SHORT:
                    writer.print("short");
                    break;
                case BYTE:
                    writer.print("byte");
                    break;
                case CHARACTER:
                    writer.print("char");
                    break;
                case DOUBLE:
                    writer.print("double");
                    break;
                case FLOAT:
                    writer.print("float");
                    break;
                case INTEGER:
                    writer.print("int");
                    break;
                case LONG:
                    writer.print("long");
                    break;
            }
        }
    }

    private void printModifiers(PrintWriter writer, ElementHolder element) {
        switch (element.getLevel()) {
            case PRIVATE:
                writer.print("private ");
                break;
            case PUBLIC:
                writer.print("public ");
                break;
            case PROTECTED:
                writer.print("protected ");
                break;
            default:
                break;
        }
        Set<ElementModifier> modifiers = element.getModifiers();
        if (modifiers.contains(ElementModifier.ABSTRACT)) {
            writer.print("abstract ");
        }
        if (modifiers.contains(ElementModifier.FINAL)) {
            writer.print("final ");
        }
        if (modifiers.contains(ElementModifier.STATIC)) {
            writer.print("static ");
        }
        if (modifiers.contains(ElementModifier.NATIVE)) {
            writer.print("native ");
        }
    }

    public void build(File dir, String fileName) throws RenderingException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(new File(dir, fileName)), "UTF-8")) {
            build(writer, new DirectoryBuildTarget(dir));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Platform does not support UTF-8", e);
        } catch (IOException e) {
            throw new RenderingException("IO error occured", e);
        }
    }

    public void installPlugins() {
        for (TeaVMPlugin plugin : ServiceLoader.load(TeaVMPlugin.class, classLoader)) {
            plugin.install(this);
        }
    }
}