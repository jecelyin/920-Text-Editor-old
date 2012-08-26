/**
 *   920 Text Editor is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   920 Text Editor is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */


#ifndef GLOBAL_H_
#define GLOBAL_H_

//调试android时需要打开
//#define ANDROID_JNI

#include <unistd.h>
#include <stdio.h> //包含NULL
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define NUL     '\000'
#define FALSE	0
#define TRUE	1
typedef unsigned long long_u;
typedef unsigned char char_u;

#define DEBUGFMT  "\n== EMSG2 ===================\n%s(%d):\n## %s:\n%s"
#define DEBUGARGS __FILE__,__LINE__
#ifndef ANDROID_JNI
	#define EMSG(s)    printf((char *)(s))
	#define EMSG2(s, p)     printf(DEBUGFMT, __FILE__, __LINE__, (char *)(s), (char *)(p))
	#define LOGV(...) //printf(__VA_ARGS__)
#else
	#include <android/log.h>
	#define APPNAME "920TextEditorJNI"
	#define EMSG(s)    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, (s));
	#define EMSG2(s, p)     __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, DEBUGFMT, __FILE__, __LINE__, (char *)(s), (char *)(p))
	#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, __VA_ARGS__)
#endif
//是否空白字符
#define ISWHITE(x)	((x) == ' ' || (x) == '\t')
//是否换行符
#define ISBREAK(x)	((x) == '\n' || (x) == '\r')
//是否是关键字
# define ISKEYWORD(c) (isalnum(c) || (c) == '_') //(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || ((c >= '0') && (c <= '9')))
//替代标准类库函数
#define STRLEN(s)	    strlen((char *)(s))
#define STRCAT(d, s)	    strcat((char *)(d), (char *)(s))
#define STRCPY(d, s)	    strcpy((char *)(d), (char *)(s))
#define STRNCPY(d, s, n)    strncpy((char *)(d), (char *)(s), (size_t)(n))
#define STRCMP(d, s)	    strcmp((char *)(d), (char *)(s))
#define STRNCMP(d, s, n)    strncmp((char *)(d), (char *)(s), (size_t)(n))
#define STRCASECMP(d, s)    strcasecmp((char *)(d), (char *)(s))
#define STRNCASECMP(d, s, n)    strncasecmp((char *)(d), (char *)(s), (size_t)(n))

#include "structs.h"
#include "hashtab.h"
#include "utils.h"
#include "misc2.h"
#include "mbyte.h"
#include "file.h"
#include "syntax.h"


#endif /* GLOBAL_H_ */
