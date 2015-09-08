package org.teavm.classlib.java.io;

import org.apache.harmony.luni.platform.FSNativeGenerator;
import org.apache.harmony.luni.platform.JSFileStat;
import org.apache.harmony.luni.platform.JSFileStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.dom.typedarrays.ArrayBufferView;
import org.teavm.dom.typedarrays.Int8Array;
import org.teavm.dom.typedarrays.TypedArrayFactory;
import org.teavm.javascript.spi.GeneratedBy;
import org.teavm.jso.JS;
import org.teavm.jso.JSBody;

public class TEmscriptenFileSystem implements TIFileSystem {

//	@JSBody(params={"path", "mode"}, script="return FS.mkdir(path, mode);")
//	@JSBody(params={"oldpath", "newpath"}, script="return FS.rename(oldpath, newpath);")
//	@JSBody(params={"path"}, script="return FS.rmdir(path);")
//	@JSBody(params={"path"}, script="return FS.rmdir(path);")
	
	static final TypedArrayFactory arrayFactory = (TypedArrayFactory) JS.getGlobal();

	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_mkdir(String path);

	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_rename(String oldPath, String newPath);

	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_rmdir(String path);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_unlink(String path);

	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_truncate(String path, int len);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_utime(String path, int atime, int mtime);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_open(String path, String flags, int mode);

	@GeneratedBy(FSNativeGenerator.class)
	public static native JSFileStream FS_open(String path, String flags);

	@GeneratedBy(FSNativeGenerator.class)
	public static native void FS_close(JSFileStream stream);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native int FS_llseek(JSFileStream stream, int offset, int whence);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native int FS_read(JSFileStream stream, ArrayBufferView buffer, int offset, int length, int position);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native int FS_read(JSFileStream stream, ArrayBufferView buffer, int offset, int length);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native int FS_write(JSFileStream stream, ArrayBufferView buffer, int offset, int length, int position);

	@GeneratedBy(FSNativeGenerator.class)
	public static native int FS_write(JSFileStream stream, ArrayBufferView buffer, int offset, int length);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native JSFileStat FS_stat(String path);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native boolean FS_isFile(int mode);

	@GeneratedBy(FSNativeGenerator.class)
	public static native boolean FS_isDir(int mode);
	
	@GeneratedBy(FSNativeGenerator.class)
	public static native String FS_cwd();

	@GeneratedBy(FSNativeGenerator.class)
	public static native String[] FS_readdir(String path);
	
	@JSBody(params={"path"}, script="try { return FS.lookupPath(path).node; } catch (e) {} return false; ")
	public static native boolean _FS_exists(String path);
	

	@Override
	public int read(TFileDescriptor fileDescriptor, byte[] bytes, int offset, int length) throws TIOException {
		final Int8Array ba = tempBuffer(length);
		final int n = FS_read(fileDescriptor.descriptor, ba, offset, length);
		readBytes(ba, bytes, offset, n);
		return n;
	}

	Int8Array tmpArray;
	private Int8Array tempBuffer(int length) {
		if (tmpArray == null || tmpArray.getLength() < length) {
			final int len = Math.min(length, 4096);
			
			if (len > 64 * 1024) {
				throw new TIOException(TString.wrap("Exceeded max buffer size " + length + " > 64k"));
			}
			
			tmpArray = arrayFactory.createInt8Array(len);
		}
		return tmpArray;
	}

	static void readBytes(final Int8Array ba, byte[] bytes, int offset, final int n) {
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				bytes[offset + i] = ba.get(i);
			}
		}
	}

	static void writeBytes(final Int8Array ba, byte[] bytes, int offset, final int n) {
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				ba.set(i, bytes[offset + i]);
			}
		}
	}
	
	@Override
	public int write(TFileDescriptor fileDescriptor, byte[] bytes, int offset, int length) throws TIOException {
		final Int8Array ba = tempBuffer(length);
		writeBytes(ba, bytes, offset, length);
		final int n = FS_write(fileDescriptor.descriptor, ba, offset, length);
		return n;
	}

	@Override
	public int readv(TFileDescriptor fileDescriptor, int[] addresses, int[] offsets, int[] lengths, int size) throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int writev(TFileDescriptor fileDescriptor, Object[] buffers, int[] offsets, int[] lengths, int size) throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readDirect(TFileDescriptor fileDescriptor, int address, int offset, int length) throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int writeDirect(TFileDescriptor fileDescriptor, int address, int offset, int length) throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean lock(TFileDescriptor fileDescriptor, int start, int length, int type, boolean waitFlag) throws TIOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unlock(TFileDescriptor fileDescriptor, int start, int length) throws TIOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int seek(TFileDescriptor fileDescriptor, int offset, int whence) throws TIOException {
		return FS_llseek(fileDescriptor.descriptor, offset, whence >> 1);
	}

	@Override
	public void fflush(TFileDescriptor fileDescriptor, boolean metadata) throws TIOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void close(TFileDescriptor fileDescriptor) throws TIOException {
		FS_close(fileDescriptor.descriptor);
	}

	@Override
	public void truncate(TFileDescriptor fileDescriptor, int size) throws TIOException {
		FS_truncate(fileDescriptor.filename, size);
	}

	@Override
	public int getAllocGranularity() throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void open(TFileDescriptor fileDescriptor, int mode) throws TFileNotFoundException {
		
		final String flags;
		if (isFlagSet(mode, O_RDONLY)) {
			flags = "r";
		} else if (isFlagSet(mode, O_APPEND)) {
			flags = "w+";
		} else if (isFlagSet(mode, O_WRONLY)) {
			flags = "w";
		} else {
			throw new TIOException(TString.wrap("Invalid open mode 0x" + Integer.toHexString(mode)));
		}
		
		fileDescriptor.descriptor = FS_open(fileDescriptor.filename, flags);
		
		if (fileDescriptor.descriptor == null) {
			throw new TIOException(TString.wrap("Error opening file " + fileDescriptor.filename));
		}
	}

	private boolean isFlagSet(int mode, final int flag) {
		return (mode & flag) != 0;
	}

	@Override
	public int transfer(int fileHandler, TFileDescriptor socketDescriptor, int offset, int count) throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int ttyAvailable() throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int available(TFileDescriptor fileDescriptor) throws TIOException {
		final int curr = FS_llseek(fileDescriptor.descriptor, 0, SEEK_CUR >> 1);
		final int end = FS_llseek(fileDescriptor.descriptor, 0, SEEK_END >> 1);
		FS_llseek(fileDescriptor.descriptor, curr, SEEK_SET >> 1);
		return end - curr;
	}

	@Override
	public int size(TFileDescriptor fileDescriptor) throws TIOException {
		return FS_stat(fileDescriptor.filename).getSize();
	}

	@Override
	public int ttyRead(byte[] bytes, int offset, int length) throws TIOException {
		// TODO Auto-generated method stub
		return 0;
	}



}
