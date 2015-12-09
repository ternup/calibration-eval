/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.model;

import org.akvo.caddisfly.app.CaddisflyApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.akvo.caddisfly.util.Color;

/**
 * Model to hold test configuration information
 */
public class TestInfo {
    private final Hashtable names;
    private final String code;
    private final String unit;
    private ArrayList<Swatch> swatches;
    private final CaddisflyApp.TestType testType;
    private final ArrayList<Integer> dilutions;
    private final boolean requiresCalibration;
    private boolean mIsDirty;

    public TestInfo(Hashtable names, String code, String unit, CaddisflyApp.TestType testType,
                    boolean requiresCalibration, String[] swatchArray, String[] dilutionsArray) {
        this.names = names;
        this.testType = testType;
        this.code = code;
        this.unit = unit;
        swatches = new ArrayList<>();
        dilutions = new ArrayList<>();
        this.requiresCalibration = requiresCalibration;

        for (String range : swatchArray) {
            Swatch swatch = new Swatch(((int) (Double.valueOf(range) * 10)) / 10f,
                    Color.TRANSPARENT, Color.TRANSPARENT);
            addSwatch(swatch);
        }

        for (String dilution : dilutionsArray) {
            addDilution(Integer.parseInt(dilution));
        }
    }

    public TestInfo() {
        names = null;
        testType = CaddisflyApp.TestType.COLORIMETRIC_LIQUID;
        code = "";
        unit = "";
        swatches = new ArrayList<>();
        dilutions = new ArrayList<>();
        this.requiresCalibration = false;
    }

    /**
     * Sort the swatches for this test by their result values
     */
    private void sort() {
        Collections.sort(swatches, new Comparator<Swatch>() {
            public int compare(Swatch c1, Swatch c2) {
                return Double.compare(c1.getValue(), (c2.getValue()));
            }
        });
    }

    public String getName(String languageCode) {
        if (names != null) {
            if (names.containsKey(languageCode)) {
                return names.get(languageCode).toString();
            } else if (names.containsKey("en")) {
                return names.get("en").toString();
            }
        }
        return "";
    }

    public CaddisflyApp.TestType getType() {
        return testType;
    }

    public String getCode() {
        return code;
    }

    public String getUnit() {
        return unit;
    }

    public ArrayList<Swatch> getSwatches() {
        //ensure that swatches is always sorted
        if (mIsDirty) {
            mIsDirty = false;
            sort();
        }
        return swatches;
    }

    public double getDilutionRequiredLevel() {
        Swatch swatch = swatches.get(swatches.size() - 1);
        return swatch.getValue() - 0.2;
    }

    public void addSwatch(Swatch value) {
        swatches.add(value);
        mIsDirty = true;
    }

    public Swatch getSwatch(int position) {
        return swatches.get(position);
    }

    private void addDilution(int dilution) {
        dilutions.add(dilution);
    }

    public boolean getCanUseDilution() {
        return dilutions.size() > 1;
    }

    /**
     * Gets if this test type requires calibration
     *
     * @return true if calibration required
     */
    public boolean getRequiresCalibration() {
        return requiresCalibration;
    }

    public void setRanges(ArrayList<Swatch> swatches) {
        this.swatches = swatches;
    }
}
