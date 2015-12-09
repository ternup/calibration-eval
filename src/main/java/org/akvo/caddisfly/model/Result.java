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

import java.util.ArrayList;

public class Result {
    private final Bitmap bitmap;
    private final ArrayList<ResultDetail> results;

    public Result(Bitmap bitmap, ArrayList<ResultDetail> results) {
        this.bitmap = bitmap;
        this.results = results;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public ArrayList<ResultDetail> getResults() {
        return results;
    }
}
