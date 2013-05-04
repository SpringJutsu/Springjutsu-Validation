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

package org.springjutsu.validation.mvc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When applied to a method, and a SuccessViewHandlerInterceptor instance
 * is configured with the application context, indicates the view name which
 * should be applied after the successful completion of the method invocation.
 * The sourceUrl object can be used to qualify multiple target view names based
 * off of varying source urls by providing a single SuccessView annotation for
 * each source url to view name mapping to the value of a SuccessViews annotation.
 * @author Clark Duplichien
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SuccessView {
	public String sourceUrl() default "";
	public String value();
}
