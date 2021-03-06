/*
 * Copyright (C) 2021 Manuel Di Donna
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  he Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.manueldidonna.godottrains.database

import android.content.Context
import com.manueldidonna.godottrains.AppDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver

object SqlDelightDatabase {

    private var database: AppDatabase? = null

    fun init(context: Context) {
        if (database != null) return
        database = AppDatabase(
            driver = AndroidSqliteDriver(
                schema = AppDatabase.Schema,
                context = context,
                name = "godot_trains.db"
            )
        )
    }

    val getStationNamesUseCase by lazy {
        GetStationNamesUseCase(database!!.trainStationQueries)
    }

    val saveStationNameUseCase by lazy {
        SaveStationNameUseCase(database!!.trainStationQueries)
    }
}
