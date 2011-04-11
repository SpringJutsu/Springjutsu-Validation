/*
 * Copyright 2010-2011 Duplichien, Wicksell, Springjutsu.org
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

package org.springjutsu.validation.mvc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When annotating a controller method, the value
 * is a string that indicates the view that should follow
 * a successful completion of the method, overriding any previously
 * evaluated view names.
 * Useful when you want your service and controller methods to be synonomous,
 * because it allows a preferred view to be set regardless of return type.
 * View name may contain restful variable syntax, where "{foo}" will be replaced
 * by by some property named "foo" from the model and view object. 
 * If a single controller method handles multiple request paths, the success view can
 * be wired dependent on the path by specifying "someInputPath=someSuccessPath", in an array.
 * Requires that AnnotatedDefaultViewSettingHandlerInterceptor be a wired bean.
 * @author Clark Duplichien
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SuccessView {	
	public String[] value();
}
