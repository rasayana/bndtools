/*******************************************************************************
 * Copyright (c) 2010 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 *******************************************************************************/
package bndtools.model.importanalysis;

import java.util.Collection;


public class ImportsAndExports {
	final Collection<? extends ImportPackage> imports;
	final Collection<? extends ExportPackage> exports;

	public ImportsAndExports(Collection<? extends ImportPackage> imports, Collection<? extends ExportPackage> exports) {
		this.imports = imports;
		this.exports = exports;
	}
}