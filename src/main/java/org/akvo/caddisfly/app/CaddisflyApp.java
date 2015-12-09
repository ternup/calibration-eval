/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.app;

import org.akvo.caddisfly.model.TestInfo;

public class CaddisflyApp {

    private static CaddisflyApp app;// Singleton
    private TestInfo mCurrentTestInfo = new TestInfo();

    /**
     * Gets the singleton app object
     *
     * @return the singleton app
     */
    public static CaddisflyApp getApp() {
        return app;
    }


    /**
     * Gets the current TestInfo
     *
     * @return the current test info
     */
    public TestInfo getCurrentTestInfo() {
        return mCurrentTestInfo;
    }

    public void setCurrentTestInfo(TestInfo testInfo) {
        mCurrentTestInfo = testInfo;
    }

    /**
     * The different types of testing methods
     */
    public enum TestType {
        /**
         * Liquid reagent is mixed with sample and color is analysed from the resulting
         * color change in the solution
         */
        COLORIMETRIC_LIQUID,

        /**
         * Strip paper is dipped into the sample and color is analysed from the resulting
         * color change on the strip paper
         */
        COLORIMETRIC_STRIP,

        /**
         * External sensors connected to the phone/device
         */
        SENSOR,

        /**
         * Measure of turbidity in the liquid
         */
        TURBIDITY_COLIFORMS
    }

}
