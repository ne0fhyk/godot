/*************************************************************************/
/*  dir_access_jandroid.cpp                                              */
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

#include "dir_access_jandroid.h"
#include "core/print_string.h"
#include "file_access_android.h"
#include "string_android.h"
#include "thread_jandroid.h"

jobject DirAccessJAndroid::storage_handler = nullptr;
jclass DirAccessJAndroid::cls = nullptr;
jmethodID DirAccessJAndroid::_dir_open = nullptr;
jmethodID DirAccessJAndroid::_dir_next = nullptr;
jmethodID DirAccessJAndroid::_dir_close = nullptr;
jmethodID DirAccessJAndroid::_dir_is_dir = nullptr;
jmethodID DirAccessJAndroid::_get_drive_count = nullptr;
jmethodID DirAccessJAndroid::_get_drive = nullptr;
jmethodID DirAccessJAndroid::_make_dir = nullptr;
jmethodID DirAccessJAndroid::_get_space_left = nullptr;
jmethodID DirAccessJAndroid::_get_filesystem_type = nullptr;
jmethodID DirAccessJAndroid::_rename = nullptr;
jmethodID DirAccessJAndroid::_remove = nullptr;

DirAccess *DirAccessJAndroid::create_fs() {
	return memnew(DirAccessJAndroid);
}

Error DirAccessJAndroid::list_dir_begin() {
	list_dir_end();
	int res = dir_open(current_dir);
	if (res <= 0)
		return ERR_CANT_OPEN;

	id = res;

	return OK;
}

String DirAccessJAndroid::get_next() {
	ERR_FAIL_COND_V(id == 0, "");
	if (_dir_next) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env == nullptr, "");
		jstring str = (jstring)env->CallObjectMethod(storage_handler, _dir_next, get_access_type(), id);
		if (!str)
			return "";

		String ret = jstring_to_string((jstring)str, env);
		env->DeleteLocalRef((jobject)str);
		return ret;
	} else {
		return "";
	}
}

bool DirAccessJAndroid::current_is_dir() const {
	if (_dir_is_dir) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env == nullptr, false);
		return env->CallBooleanMethod(storage_handler, _dir_is_dir, get_access_type(), id);
	} else {
		return false;
	}
}

bool DirAccessJAndroid::current_is_hidden() const {
	return current_dir != "." && current_dir != ".." && current_dir.begins_with(".");
}

void DirAccessJAndroid::list_dir_end() {
	if (id == 0)
		return;

	dir_close(id);
	id = 0;
}

int DirAccessJAndroid::get_drive_count() {
	if (_get_drive_count) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env == nullptr, 0);
		return env->CallIntMethod(storage_handler, _get_drive_count, get_access_type());
	} else {
		return 0;
	}
}

String DirAccessJAndroid::get_drive(int p_drive) {
	if (_get_drive) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env == nullptr, "");
		jstring j_drive = (jstring)env->CallObjectMethod(storage_handler, _get_drive, get_access_type(), p_drive);
		if (!j_drive) {
			return "";
		}

		String drive = jstring_to_string(j_drive, env);
		env->DeleteLocalRef(j_drive);
		return drive;
	} else {
		return "";
	}
}

bool DirAccessJAndroid::file_exists(String p_file) {
	String sd;
	if (current_dir == "")
		sd = p_file;
	else
		sd = current_dir.plus_file(p_file);

	FileAccessAndroid *f = memnew(FileAccessAndroid);
	bool exists = f->file_exists(sd);
	memdelete(f);

	return exists;
}

Error DirAccessJAndroid::make_dir(String p_dir) {
	if (_make_dir) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env==nullptr, ERR_UNCONFIGURED);
		jstring j_dir = env->NewStringUTF(p_dir.utf8().get_data());
		bool result = env->CallBooleanMethod(storage_handler, _make_dir, get_access_type(), j_dir);
		env->DeleteLocalRef(j_dir);
		if (result) {
			return OK;
		} else {
			return FAILED;
		}
	} else {
		return ERR_UNCONFIGURED;
	}
}

Error DirAccessJAndroid::rename(String p_from, String p_to) {
	if (_rename) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env==nullptr, ERR_UNCONFIGURED);
		jstring j_from = env->NewStringUTF(p_from.utf8().get_data());
		jstring j_to = env->NewStringUTF(p_to.utf8().get_data());
		bool result = env->CallBooleanMethod(storage_handler, _rename, get_access_type(), j_from, j_to);
		env->DeleteLocalRef(j_from);
		env->DeleteLocalRef(j_to);
		if (result) {
			return OK;
		} else {
			return FAILED;
		}
	} else {
		return ERR_UNCONFIGURED;
	}
}

Error DirAccessJAndroid::remove(String p_name) {
	if (_remove) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env==nullptr, ERR_UNCONFIGURED);
		jstring j_name = env->NewStringUTF(p_name.utf8().get_data());
		bool result = env->CallBooleanMethod(storage_handler, _remove, get_access_type(), j_name);
		env->DeleteLocalRef(j_name);
		if (result) {
			return OK;
		} else {
			return FAILED;
		}
	} else {
		return ERR_UNCONFIGURED;
	}
}

String DirAccessJAndroid::get_filesystem_type() const {
	if (_get_filesystem_type) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env==nullptr, FILESYSTEM_PREFIX);
		jstring j_type = (jstring)env->CallObjectMethod(storage_handler, _get_filesystem_type, get_access_type());
		if (!j_type) {
			return FILESYSTEM_PREFIX;
		}

		String type = jstring_to_string(j_type, env);
		env->DeleteLocalRef(j_type);
		return FILESYSTEM_PREFIX + "-" + type.to_upper();
	} else {
		return FILESYSTEM_PREFIX;
	}
}

uint64_t DirAccessJAndroid::get_space_left() {
	if (_get_space_left) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env==nullptr, 0);
		return env->CallLongMethod(storage_handler, _get_space_left, get_access_type());
	} else {
		return 0;
	}
}

void DirAccessJAndroid::setup(jobject p_storage_handler) {
	JNIEnv *env = get_jni_env();
	storage_handler = env->NewGlobalRef(p_storage_handler);

	jclass c = env->GetObjectClass(storage_handler);
	cls = (jclass)env->NewGlobalRef(c);

	_dir_open = env->GetMethodID(cls, "dirOpen", "(ILjava/lang/String;)I");
	_dir_next = env->GetMethodID(cls, "dirNext", "(II)Ljava/lang/String;");
	_dir_close = env->GetMethodID(cls, "dirClose", "(II)V");
	_dir_is_dir = env->GetMethodID(cls, "dirIsDir", "(II)Z");
	_get_drive_count = env->GetMethodID(cls, "getDriveCount", "(I)I");
	_get_drive = env->GetMethodID(cls, "getDrive", "(II)Ljava/lang/String;");
	_make_dir = env->GetMethodID(cls, "makeDir", "(ILjava/lang/String;)Z");
	_get_space_left = env->GetMethodID(cls, "getSpaceLeft", "(I)J");
	_get_filesystem_type = env->GetMethodID(cls, "getFilesystemType", "(I)Ljava/lang/String;");
	_rename = env->GetMethodID(cls, "rename", "(ILjava/lang/String;Ljava/lang/String;)Z");
	_remove = env->GetMethodID(cls, "remove", "(ILjava/lang/String;)Z");
}

DirAccessJAndroid::DirAccessJAndroid() {
	id = 0;
}

DirAccessJAndroid::~DirAccessJAndroid() {
	list_dir_end();
}

int DirAccessJAndroid::dir_open(String p_path) {
	if (_dir_open) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND_V(env == nullptr, 0);
		jstring js = env->NewStringUTF(p_path.utf8().get_data());
		int dirId = env->CallIntMethod(storage_handler, _dir_open, get_access_type(), js);
		env->DeleteLocalRef(js);
		return dirId;
	} else {
		return 0;
	}
}

void DirAccessJAndroid::dir_close(int p_id) {
	if (_dir_close) {
		JNIEnv *env = get_jni_env();
		ERR_FAIL_COND(env == nullptr);
		env->CallVoidMethod(storage_handler, _dir_close, get_access_type(), p_id);
	}
}

