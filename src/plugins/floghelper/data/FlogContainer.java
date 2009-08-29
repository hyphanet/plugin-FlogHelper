/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.floghelper.data;

import java.util.concurrent.ConcurrentHashMap;
import plugins.floghelper.data.Flog.DataType;

/**
 * This structure is stored in DB4O - THINK TWICE BEFORE CHANGING STUFF.
 * @author Artefact2
 */
public class FlogContainer {
	public ConcurrentHashMap<DataType, ConcurrentHashMap<Object, Object>> data;

	public FlogContainer() {
		this.data = new ConcurrentHashMap<DataType, ConcurrentHashMap<Object, Object>>();
	}
}
