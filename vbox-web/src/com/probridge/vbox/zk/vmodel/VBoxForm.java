package com.probridge.vbox.zk.vmodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.zkoss.bind.Form;
import org.zkoss.bind.FormExt;
import org.zkoss.bind.FormStatus;
import org.zkoss.lang.Objects;

public class VBoxForm implements Form, FormExt, Serializable {
	private static final long serialVersionUID = 1463169907348730644L;

	private final Set<String> _saveFieldNames; // field name for saving
	private final Set<String> _loadFieldNames; // field name for loading
	private final Map<String, Object> _fields; // field series -> value
	private final Map<String, Object> _initFields; // field series -> value
	private final Set<String> _dirtyFieldNames; // field name that is dirty
	private static final int INIT_CAPACITY = 32;

	private final FormStatus _status;

	public VBoxForm() {
		_fields = new LinkedHashMap<String, Object>(INIT_CAPACITY);
		_initFields = new HashMap<String, Object>(INIT_CAPACITY);
		_saveFieldNames = new LinkedHashSet<String>(INIT_CAPACITY);
		_loadFieldNames = new LinkedHashSet<String>(INIT_CAPACITY);
		_dirtyFieldNames = new HashSet<String>(INIT_CAPACITY);
		_status = new FormStatusImpl();
	}

	private class FormStatusImpl implements FormStatus, Serializable {
		private static final long serialVersionUID = 1L;

		public boolean isDirty() {
			return VBoxForm.this.isDirty();
		}
	}

	public void setField(String field, Object value) {
		_fields.put(field, value);
		final Object init = _initFields.get(field);
		if (!Objects.equals(init, value)) { // different from original
			_dirtyFieldNames.add(field);
		} else {
			_dirtyFieldNames.remove(field);
		}
	}

	public void resetDirty() {
		_initFields.putAll(_fields);
		_dirtyFieldNames.clear();
	}

	public Object getField(String field) {
		return _fields.get(field);
	}

	public Set<String> getLoadFieldNames() {
		return _loadFieldNames;
	}

	public Set<String> getSaveFieldNames() {
		return _saveFieldNames;
	}

	public Set<String> getFieldNames() {
		return _fields.keySet();
	}

	public boolean isDirty() {
		return !_dirtyFieldNames.isEmpty();
	}

	public void addLoadFieldName(String fieldName) {
		_loadFieldNames.add(fieldName);
	}

	public void addSaveFieldName(String fieldName) {
		_saveFieldNames.add(fieldName);
	}

	public String toString() {
		return new StringBuilder().append(getClass().getSimpleName()).append("@")
				.append(Integer.toHexString(hashCode()))
				// .append(",id:").append(getId())
				.append(",fields:").append(getFieldNames()).toString();
	}

	public FormStatus getStatus() {
		return _status;
	}

	public boolean isFieldDirty(String fieldName) {
		return _dirtyFieldNames.contains(fieldName);
	}
}
