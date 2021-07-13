/*************************************************************************/
/*  dir_access_jandroid.h                                                */
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

#ifndef DIR_ACCESS_JANDROID_H
#define DIR_ACCESS_JANDROID_H

#include "core/os/dir_access.h"
#include "java_godot_lib_jni.h"
#include <stdio.h>

static const String FILESYSTEM_PREFIX = "ANDROID";

class DirAccessJAndroid : public DirAccess {

	static jobject storage_handler;
	static jclass cls;

	static jmethodID _dir_open;
	static jmethodID _dir_next;
	static jmethodID _dir_close;
	static jmethodID _dir_is_dir;
	static jmethodID _get_drive_count;
	static jmethodID _get_drive;
	static jmethodID _make_dir;
	static jmethodID _get_space_left;
	static jmethodID _get_filesystem_type;
	static jmethodID _rename;
	static jmethodID _remove;

	static DirAccess *create_fs();

public:
	virtual Error list_dir_begin(); ///< This starts dir listing
	virtual String get_next();
	virtual bool current_is_dir() const;
	virtual bool current_is_hidden() const;
	virtual void list_dir_end(); ///<

	virtual int get_drive_count();
	virtual String get_drive(int p_drive);

	virtual Error change_dir(String p_dir) = 0; ///< can be relative or absolute, return false on success
	virtual String get_current_dir() = 0; ///< return current dir location

	virtual bool file_exists(String p_file);
	virtual bool dir_exists(String p_dir) = 0;

	virtual Error make_dir(String p_dir);

	virtual Error rename(String p_from, String p_to);
	virtual Error remove(String p_name);

	virtual bool is_link(String p_file) { return false; }
	virtual String read_link(String p_file) { return p_file; }
	virtual Error create_link(String p_source, String p_target) { return FAILED; }

	virtual String get_filesystem_type() const;

	virtual uint64_t get_space_left();

	static void setup(jobject p_storage_handler);

	DirAccessJAndroid();
	~DirAccessJAndroid();

protected:
	int id;

	String current_dir;

	int dir_open(String p_path);
	void dir_close(int p_id);
};

#endif // DIR_ACCESS_JANDROID_H
