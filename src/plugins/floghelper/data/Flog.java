/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.floghelper.data;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic Flog data handler.
 * data
 * |----FlogProperties
 * |----Contents
 *      |----Content
 *      |----Content
 *      |----Content
 *      |----...
 * @author Artefact2
 */
public class Flog {
	public enum DataType {
		Contents, FlogProperties
	}

	private FlogContainer e;

	public Flog() {
		this.e = new FlogContainer();
	}

	public Flog(FlogContainer e) {
		this.e = e;
	}

	public ConcurrentHashMap<Object, Object> getFlogProperties() {
		return this.e.data.get(DataType.FlogProperties);
	}
}
