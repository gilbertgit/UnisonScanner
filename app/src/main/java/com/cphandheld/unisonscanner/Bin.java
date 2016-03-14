package com.cphandheld.unisonscanner;

/**
 * Created by titan on 3/11/16.
 */
public class Bin {

    private String _name;
    private int _binId;
    private boolean _selected;

    public Bin( String name, int binId, boolean selected) {
        _selected = selected;
        _name = name;
        _binId = binId;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public int getBinId() {
        return _binId;
    }

    public void set_binId(int binId) {
        this._binId = binId;
    }

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean select) {
        this._selected = select;
    }


    @Override
    public String toString() {
        return this._name;
    }
}
