/*
 * Copyright 2010-2013 Duplichien, Wicksell, Springjutsu.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springjutsu.validation.util;


/**
 * Holds common functions, mostly to determine some
 * traits of the model being validated.
 * @author Clark Duplichien
 * @author Taylor Wicksell
 *
 */
public class ValidationRulesUtils {
	
	//TODO: Make rules for these patterns or delete them.
	public static final String DECIMAL_PATTERN = "[0-9]*\\.?[0-9]*";
	public static final String URL_PATTERN = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
	public static final int MINDIGITSINPHONE = 10; //minimum number of digits in a valid fax or phone number
	public static final int MAXDIGITSINPHONE = 14; //maximum number of digits in a valid fax or phone number
		
	/**
	 * returns the "length" of an object; 
	 * really, it's the length of the string representation.
	 * @param object the object to determine length of.
	 * @return length of object
	 */
	public static int getLength(Object object) {
		return String.valueOf(object).length();
	}
	
	/**
	 * test if the argument is empty using a null check
	 * and a string length check.
	 * @param object the object to test
	 * @return true if the object is empty
	 */
	public static boolean isEmpty(Object object) {
		return object == null || String.valueOf(object).trim().length() < 1;
	}
}
