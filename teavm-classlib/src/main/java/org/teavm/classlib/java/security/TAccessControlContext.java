/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.teavm.classlib.java.security;


/**
 * The vm vendor may choose to implement this class. The reference
 * implementation must be used if the reference implementation of
 * AccessController is used.
 * 
 * An AccessControlContext encapsulates the information which is needed by class
 * AccessController to detect if a Permission would be granted at a particular
 * point in a programs execution.
 * 
 */

public final class TAccessControlContext {

	/**
	 * Constructs a new instance of this class given an array of protection
	 * domains.
	 * 
	 */
	TAccessControlContext() {
	}

	/**
	 * Checks if the permission <code>perm</code> is allowed in this context.
	 * All ProtectionDomains must grant the permission for it to be granted.
	 * 
	 * @param perm
	 *            java.security.Permission the permission to check
	 * @exception java.security.AccessControlException
	 *                thrown when perm is not granted.
	 */
	public void checkPermission(TPermission perm) throws TAccessControlException {
	}

	/**
	 * Compares the argument to the receiver, and answers true if they represent
	 * the <em>same</em> object using a class specific comparison. In this
	 * case, they must both be AccessControlContexts and contain the same
	 * protection domains.
	 * 
	 * 
	 * @param o
	 *            the object to compare with this object
	 * @return <code>true</code> if the object is the same as this object
	 *         <code>false</code> if it is different from this object
	 * @see #hashCode
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		return true;
	}

	/**
	 * Answers an integer hash code for the receiver. Any two objects which
	 * answer <code>true</code> when passed to <code>equals</code> must
	 * answer the same value for this method.
	 * 
	 * 
	 * @return the receiver's hash
	 * 
	 * @see #equals
	 */
	public int hashCode() {
		return TAccessControlContext.class.hashCode();
	}

//	/**
//	 * Answers the DomainCombiner for the receiver.
//	 * 
//	 */
//	public DomainCombiner getDomainCombiner() {
//		SecurityManager security = System.getSecurityManager();
//		if (security != null)
//			security.checkPermission(getDomainCombiner);
//		return domainCombiner;
//	}

}
