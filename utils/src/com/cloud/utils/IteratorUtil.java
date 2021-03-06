// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class IteratorUtil {
	public static <T> Iterable<T> enumerationAsIterable(final Enumeration<T> e) {
		return new Iterable<T> () {
			public Iterator<T> iterator() {
			    return new Iterator<T>() {
			    	public boolean hasNext() {
			    		return e.hasMoreElements();
			    	}

			    	public T next() {
			    		return e.nextElement();
			    	}

			    	public void remove() {
			    		throw new UnsupportedOperationException();
			    	}
			    };
			}
		};
	}
	
	public static <T> Enumeration<T> iteratorAsEnumeration(final Iterator<T> it) {
		return new Enumeration<T>() {
		    public boolean hasMoreElements() {
		        return it.hasNext();
		    }

		    public T nextElement() {
		        return it.next();
		    }
		};
	}

    public static
    <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }
}
