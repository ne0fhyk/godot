/*************************************************************************/
/*  FileSystemStorageProvider.kt                                         */
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
import android.util.SparseArray
import java.io.File

/**
 * Common class to handle files and directories access with the internal and external filesystem.
 */
internal abstract class FileSystemStorageProvider(private val context: Context): StorageHandler.StorageProvider {

	companion object {
		private val TAG = FileSystemStorageProvider::class.java.simpleName
	}

	private data class DirData(val dirFile: File, val files: Array<File>, var current: Int = 0)

	private var lastDirId = StorageHandler.STARTING_DIR_ID
	private val dirs = SparseArray<DirData>()

	abstract fun getBaseDir(): File?

	override fun hasDirId(dirId: Int) = dirs.indexOfKey(dirId) >= 0

	override fun dirOpen(path: String): Int {
		val baseDir = getBaseDir() ?: return StorageHandler.INVALID_DIR_ID

		var dirFile = File(path)
		if (dirFile.isAbsolute) {
			// The filepath should be within the filesystem scope. If it's not, we abort.
			if (!dirFile.canonicalPath.startsWith(baseDir.canonicalPath)) {
				return StorageHandler.INVALID_DIR_ID
			}
		} else {
			dirFile = File(baseDir, path)
		}

		// Check this is a directory.
		if (!dirFile.isDirectory) {
			return StorageHandler.INVALID_DIR_ID
		}

		// Get the files in the directory
		val files = dirFile.listFiles()?: return StorageHandler.INVALID_DIR_ID

		// Create the data representing this directory
		val dirData = DirData(dirFile, files)

		dirs.put(++lastDirId, dirData)
		return lastDirId
	}

	override fun dirNext(dirId: Int): String {
		val dirData = dirs[dirId]
		if (dirData.current >= dirData.files.size) {
			dirData.current++
			return ""
		}

		return dirData.files[dirData.current++].name
	}

	override fun dirClose(dirId: Int) {
		dirs.remove(dirId)
	}

	override fun dirIsDir(dirId: Int): Boolean {
		val dirData = dirs[dirId]

		var index = dirData.current
		if (index > 0) {
			index--
		}

		if (index >= dirData.files.size) {
			return false
		}

		return dirData.files[index].isDirectory
	}
}
