/*************************************************************************/
/*  AssetsStorageHandler.kt                                              */
/*************************************************************************/
/*                       This file is part of:                           */
/*                           GODOT ENGINE                                */
/*                      https://godotengine.org                          */
/*************************************************************************/
/* Copyright (c) 2007-2021 Juan Linietsky, Ariel Manzur.                 */
/* Copyright (c) 2014-2021 Godot Engine contributors (cf. AUTHORS.md).   */
/*                                                                       */
/* Permission is hereby granted, free of charge, to any person obtaining */
/* a copy of this software and associated documentation files (the       */
/* "Software"), to deal in the Software without restriction, including   */
/* without limitation the rights to use, copy, modify, merge, publish,   */
/* distribute, sublicense, and/or sell copies of the Software, and to    */
/* permit persons to whom the Software is furnished to do so, subject to */
/* the following conditions:                                             */
/*                                                                       */
/* The above copyright notice and this permission notice shall be        */
/* included in all copies or substantial portions of the Software.       */
/*                                                                       */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */
/*************************************************************************/

package org.godotengine.godot.io

import android.content.Context
import android.util.Log
import android.util.SparseArray
import org.godotengine.godot.io.StorageHandler.Companion.INVALID_DIR_ID
import org.godotengine.godot.io.StorageHandler.Companion.STARTING_DIR_ID
import java.io.IOException

/**
 * Handles files and directories access with the assets filesystem.
 */
internal class AssetsStorageHandler(context: Context) : StorageHandler.StorageProvider {

	companion object {
		private val TAG = AssetsStorageHandler::class.java.simpleName
	}

	private data class AssetDir(val path: String, val files: Array<String>, var current: Int = 0)

	private val assetManager = context.assets

	private var lastDirId = STARTING_DIR_ID
	private val dirs: SparseArray<AssetDir> = SparseArray()

	override fun hasDirId(dirId: Int) = dirs.indexOfKey(dirId) >= 0

	override fun dirOpen(path: String): Int {
		try {
			val files = assetManager.list(path) ?: return INVALID_DIR_ID
			// Empty directories don't get added to the 'assets' directory, so
			// if ad.files.length > 0 ==> path is directory
			// if ad.files.length == 0 ==> path is file
			if (files.isEmpty()) {
				return INVALID_DIR_ID
			}

			val ad = AssetDir(path, files)

			dirs.put(++lastDirId, ad)
			return lastDirId
		} catch (e: IOException) {
			Log.e(TAG, "Exception on dirOpen", e)
			return INVALID_DIR_ID
		}
	}

	override fun dirIsDir(dirId: Int): Boolean {
		val ad: AssetDir = dirs[dirId]

		var idx = ad.current
		if (idx > 0) {
			idx--
		}

		if (idx >= ad.files.size) {
			return false
		}

		val fileName = ad.files[idx]
		// List the contents of $fileName. If it's a file, it will be empty, otherwise it'll be a
		// directory
		val filePath = if (ad.path == "") fileName else "${ad.path}/${fileName}"
		val fileContents = assetManager.list(filePath)
		return (fileContents?.size?: 0) > 0
	}

	override fun dirNext(dirId: Int): String {
		val ad: AssetDir = dirs[dirId]

		if (ad.current >= ad.files.size) {
			ad.current++
			return ""
		}

		return ad.files[ad.current++]
	}

	override fun dirClose(dirId: Int) {
		dirs.remove(dirId)
	}
}
