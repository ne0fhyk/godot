/*************************************************************************/
/*  dir_access_resources_jandroid.cpp                                    */
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

#include "dir_access_resources_jandroid.h"
#include "core/print_string.h"
#include "file_access_android.h"
#include "string_android.h"
#include "thread_jandroid.h"

DirAccess *DirAccessResourcesJAndroid::create_fs() {
	return memnew(DirAccessResourcesJAndroid);
}

int DirAccessResourcesJAndroid::get_drive_count() {
	return 0;
}

String DirAccessResourcesJAndroid::get_drive(int p_drive) {
	return "";
}

Error DirAccessResourcesJAndroid::change_dir(String p_dir) {
	if (p_dir == "" || p_dir == "." || (p_dir == ".." && current_dir == ""))
		return OK;

	String new_dir;

	if (p_dir != "res://" && p_dir.length() > 1 && p_dir.ends_with("/"))
		p_dir = p_dir.substr(0, p_dir.length() - 1);

	if (p_dir.begins_with("/"))
		new_dir = p_dir.substr(1, p_dir.length());
	else if (p_dir.begins_with("res://"))
		new_dir = p_dir.substr(6, p_dir.length());
	else if (current_dir == "")
		new_dir = p_dir;
	else
		new_dir = current_dir.plus_file(p_dir);

	//test if newdir exists
	new_dir = new_dir.simplify_path();

	int res = dir_open(new_dir);
	if (res <= 0)
		return ERR_INVALID_PARAMETER;

	dir_close(res);

	current_dir = new_dir;

	return OK;
}

String DirAccessResourcesJAndroid::get_current_dir() {
	return "res://" + current_dir;
}

bool DirAccessResourcesJAndroid::dir_exists(String p_dir) {
	String sd;

	if (current_dir == "")
		sd = p_dir;
	else {
		if (p_dir.is_rel_path())
			sd = current_dir.plus_file(p_dir);
		else
			sd = fix_path(p_dir);
	}

	String path = sd.simplify_path();

	if (path.begins_with("/"))
		path = path.substr(1, path.length());
	else if (path.begins_with("res://"))
		path = path.substr(6, path.length());

	int res = dir_open(path);
	if (res <= 0)
		return false;

	dir_close(res);

	return true;
}

Error DirAccessResourcesJAndroid::make_dir(String p_dir) {
	ERR_FAIL_V(ERR_UNAVAILABLE);
}

Error DirAccessResourcesJAndroid::rename(String p_from, String p_to) {
	ERR_FAIL_V(ERR_UNAVAILABLE);
}

Error DirAccessResourcesJAndroid::remove(String p_name) {
	ERR_FAIL_V(ERR_UNAVAILABLE);
}

String DirAccessResourcesJAndroid::get_filesystem_type() const {
	return FILESYSTEM_PREFIX + "-ASSETS";
}

uint64_t DirAccessResourcesJAndroid::get_space_left() {
	return 0;
}

void DirAccessResourcesJAndroid::setup(jobject p_io) {
	DirAccessJAndroid::setup(p_io);
}
