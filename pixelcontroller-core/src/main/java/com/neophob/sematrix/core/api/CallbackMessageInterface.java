/**
 * Copyright (C) 2011-2014 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.sematrix.core.api;

import java.util.Observer;

/**
 * observer pattern implementation for client callback
 * 
 * the client must implement the type checking, for example
 * 
 * 	@Override
 *	public void update(Observable o, Object arg) {
 *		//hint the generics type T is only available during compile time (not during runtime)
 *		//thats why an instanceof will not work, make it simple here, cast or fail.
 *		try {
 *			T msg = (T) arg;
 *			handleMessage(msg);			
 *		} catch (Exception e) {
 *			String className = arg==null ? "" : ""+arg.getClass();
 *			LOG.log(Level.WARNING, "Ignored notification of unknown class: "+className+", content: "+arg);			
 *		}
 *	}
 * 
 * @author michu
 *
 */
public interface CallbackMessageInterface<T> extends Observer {

	void handleMessage(T msg);
	
}
