/*************************************************************************/
/*  StorageHandler.kt                                                    */
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

/**
 * Handles files and directories access and manipulation for the Android platform
 */
class StorageHandler(context: Context) {

	companion object {
		private val TAG = StorageHandler::class.java.simpleName

		private const val ACCESS_RESOURCES = 0
		private const val ACCESS_USERDATA = 1
		private const val ACCESS_FILESYSTEM = 2

		internal const val INVALID_DIR_ID = -1
		internal const val STARTING_DIR_ID = 1
	}

	internal interface StorageProvider {
		fun dirOpen(path: String): Int
		fun dirNext(dirId: Int): String
		fun dirClose(dirId: Int)
		fun dirIsDir(dirId: Int): Boolean
		fun hasDirId(dirId: Int): Boolean
	}

	private val assetsStorageHandler = AssetsStorageHandler(context)
	private val internalStorageHandler = InternalStorageHandler(context)
	private val externalStorageHandler = ExternalStorageHandler(context)

	private fun hasDirId(accessType: Int, dirId: Int): Boolean {
		return when (accessType) {
			ACCESS_RESOURCES -> assetsStorageHandler.hasDirId(dirId)
			ACCESS_USERDATA -> internalStorageHandler.hasDirId(dirId)
			ACCESS_FILESYSTEM -> externalStorageHandler.hasDirId(dirId)
			else -> externalStorageHandler.hasDirId(dirId)
		}
	}

	fun dirOpen(accessType: Int, path: String?): Int {
		if (path == null) {
			return INVALID_DIR_ID
		}

		return when (accessType) {
			ACCESS_RESOURCES -> assetsStorageHandler.dirOpen(path)
			ACCESS_USERDATA -> internalStorageHandler.dirOpen(path)
			ACCESS_FILESYSTEM -> externalStorageHandler.dirOpen(path)
			else -> externalStorageHandler.dirOpen(path)
		}
	}

	fun dirNext(accessType: Int, dirId: Int): String {
		if (!hasDirId(accessType, dirId)) {
			Log.w(TAG, "dirNext: Invalid dir id: $dirId")
			return ""
		}

		return when (accessType) {
			ACCESS_RESOURCES -> assetsStorageHandler.dirNext(dirId)
			ACCESS_USERDATA -> internalStorageHandler.dirNext(dirId)
			ACCESS_FILESYSTEM -> externalStorageHandler.dirNext(dirId)
			else -> externalStorageHandler.dirNext(dirId)
		}
	}

	fun dirClose(accessType: Int, dirId: Int) {
		if (!hasDirId(accessType, dirId)) {
			Log.w(TAG, "dirClose: Invalid dir id: $dirId")
			return
		}

		when (accessType) {
			ACCESS_RESOURCES -> assetsStorageHandler.dirClose(dirId)
			ACCESS_USERDATA -> internalStorageHandler.dirClose(dirId)
			ACCESS_FILESYSTEM -> externalStorageHandler.dirClose(dirId)
			else -> externalStorageHandler.dirClose(dirId)
		}
	}

	fun dirIsDir(accessType: Int, dirId: Int): Boolean {
		if (!hasDirId(accessType, dirId)) {
			Log.w(TAG, "dirIsDir: Invalid dir id: $dirId")
			return false
		}

		return when (accessType) {
			ACCESS_RESOURCES -> assetsStorageHandler.dirIsDir(dirId)
			ACCESS_USERDATA -> internalStorageHandler.dirIsDir(dirId)
			ACCESS_FILESYSTEM -> externalStorageHandler.dirIsDir(dirId)
			else -> externalStorageHandler.dirIsDir(dirId)
		}
	}

	fun getDriveCount(accessType: Int): Int {
		TODO("Complete implementation")
	}

	fun getDrive(accessType: Int, drive: Int): String {
		TODO("Complete implementation")
	}

	fun makeDir(accessType: Int, dir: String): Boolean {
		TODO("Complete implementation")
	}

	fun getSpaceLeft(accessType: Int): Long {
		TODO("Complete implementation")
	}

	fun getFilesystemType(accessType: Int): String {
		TODO("Complete implementation")
	}

	fun rename(accessType: Int, from: String, to: String): Boolean {
		TODO("Complete implementation")
	}

	fun remove(accessType: Int, filename: String): Boolean {
		TODO("Complete implementation")
	}

}
