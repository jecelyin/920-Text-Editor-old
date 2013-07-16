#ifndef LIBBB_H
#define LIBBB_H 1

#include <ctype.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <netdb.h>
#include <setjmp.h>
#include <signal.h>
#if defined __UCLIBC__ /* TODO: and glibc? */
/* use inlined versions of these: */
# define sigfillset(s)    __sigfillset(s)
# define sigemptyset(s)   __sigemptyset(s)
# define sigisemptyset(s) __sigisemptyset(s)
#endif
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stddef.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>

/* There are two incompatible basename's, let not use them! */
/* See the dirname/basename man page for details */
#include <libgen.h> /* dirname,basename */
#undef basename
#define basename dont_use_basename

#include "platform.h"

#define IF_FEATURE_GREP_CONTEXT(...)
#define IF_FEATURE_GREP_EGREP_ALIAS(...)
#define IF_EXTRA_COMPAT(...)

#ifndef _GNU_SOURCE
#define _GNU_SOURCE  //警告： 隐式声明函数‘strcasestr’ [-Wimplicit-function-declaration]
#endif
#ifndef __USE_GNU
#define __USE_GNU  //解决RE_TRANSLATE_TYPE变量没定义的问题
#endif

/* ISO C Standard:  7.16  Boolean type and values  <stdbool.h> */
#if (defined __digital__ && defined __unix__)
/* old system without (proper) C99 support */
# define bool smalluint
#else
/* modern system, so use it */
# include <stdbool.h>
#endif

/* Having next pointer as a first member allows easy creation
 * of "llist-compatible" structs, and using llist_FOO functions
 * on them.
 */
typedef struct llist_t {
    struct llist_t *link;
    char *data;
} llist_t;

#ifndef BUFSIZ
# define BUFSIZ 4096
#endif
/* Providing hard guarantee on minimum size (think of BUFSIZ == 128) */
enum { COMMON_BUFSIZE = (BUFSIZ >= 256*sizeof(void*) ? BUFSIZ+1 : 256*sizeof(void*)) };
extern char bb_common_bufsiz1[COMMON_BUFSIZE];

enum {
    ACTION_RECURSE        = (1 << 0),
    ACTION_FOLLOWLINKS    = (1 << 1),
    ACTION_FOLLOWLINKS_L0 = (1 << 2),
    ACTION_DEPTHFIRST     = (1 << 3),
    /*ACTION_REVERSE      = (1 << 4), - unused */
    ACTION_QUIET          = (1 << 5),
    ACTION_DANGLING_OK    = (1 << 6),
};

/* ignored: -a "assume all files to be text" */
/* ignored: -I "assume binary files have no matches" */
enum {
	OPTBIT_l, /* list matched file names only */
	OPTBIT_n, /* print line# */
	OPTBIT_q, /* quiet - exit(EXIT_SUCCESS) of first match */
	OPTBIT_v, /* invert the match, to select non-matching lines */
	OPTBIT_s, /* suppress errors about file open errors */
	OPTBIT_c, /* count matches per file (suppresses normal output) */
	OPTBIT_F, /* literal match */
	OPTBIT_i, /* case-insensitive */
	OPTBIT_H, /* force filename display */
	OPTBIT_h, /* inhibit filename display */
	OPTBIT_e, /* -e PATTERN */
	OPTBIT_f, /* -f FILE_WITH_PATTERNS */
	OPTBIT_L, /* list unmatched file names only */
	OPTBIT_o, /* show only matching parts of lines */
	OPTBIT_r, /* recurse dirs */
	OPTBIT_m, /* -m MAX_MATCHES */
	OPTBIT_w, /* -w whole word match */
	OPTBIT_x, /* -x whole line match */
	IF_FEATURE_GREP_CONTEXT(    OPTBIT_A ,) /* -A NUM: after-match context */
	IF_FEATURE_GREP_CONTEXT(    OPTBIT_B ,) /* -B NUM: before-match context */
	IF_FEATURE_GREP_CONTEXT(    OPTBIT_C ,) /* -C NUM: -A and -B combined */
	//IF_FEATURE_GREP_EGREP_ALIAS(OPTBIT_E ,) /* extended regexp */
	OPTBIT_E, /* extended regexp */
	IF_EXTRA_COMPAT(            OPTBIT_z ,) /* input is NUL terminated */
	OPT_l = 1 << OPTBIT_l,
	OPT_n = 1 << OPTBIT_n,
	OPT_q = 1 << OPTBIT_q,
	OPT_v = 1 << OPTBIT_v,
	OPT_s = 1 << OPTBIT_s,
	OPT_c = 1 << OPTBIT_c,
	OPT_F = 1 << OPTBIT_F,
	OPT_i = 1 << OPTBIT_i,
	OPT_H = 1 << OPTBIT_H,
	OPT_h = 1 << OPTBIT_h,
	OPT_e = 1 << OPTBIT_e,
	OPT_f = 1 << OPTBIT_f,
	OPT_L = 1 << OPTBIT_L,
	OPT_o = 1 << OPTBIT_o,
	OPT_r = 1 << OPTBIT_r,
	OPT_m = 1 << OPTBIT_m,
	OPT_w = 1 << OPTBIT_w,
	OPT_x = 1 << OPTBIT_x,
	OPT_A = IF_FEATURE_GREP_CONTEXT(    (1 << OPTBIT_A)) + 0,
	OPT_B = IF_FEATURE_GREP_CONTEXT(    (1 << OPTBIT_B)) + 0,
	OPT_C = IF_FEATURE_GREP_CONTEXT(    (1 << OPTBIT_C)) + 0,
	//OPT_E = IF_FEATURE_GREP_EGREP_ALIAS((1 << OPTBIT_E)) + 0,
	OPT_E = (1 << OPTBIT_E) + 0,
	OPT_z = IF_EXTRA_COMPAT(            (1 << OPTBIT_z)) + 0,
};

#if ENABLE_FEATURE_INDIVIDUAL
#define MAIN_EXTERNALLY_VISIBLE EXTERNALLY_VISIBLE
#else
#define MAIN_EXTERNALLY_VISIBLE
#endif

#undef FALSE
#define FALSE   ((int) 0)
#undef TRUE
#define TRUE    ((int) 1)
#undef SKIP
#define SKIP    ((int) 2)

#define ENABLE_FEATURE_GREP_EGREP_ALIAS 1
#define ENABLE_FEATURE_CLEAN_UP 1
#define ENABLE_FEATURE_GREP_FGREP_ALIAS 0
#define ENABLE_EXTRA_COMPAT 0

#define NOT_LONE_DASH(s) ((s)[0] != '-' || (s)[1])
#define DOT_OR_DOTDOT(s) ((s)[0] == '.' && (!(s)[1] || ((s)[1] == '.' && !(s)[2])))
#define LONE_DASH(s)     ((s)[0] == '-' && !(s)[1])

/* Reads up to (and including) "\n" or NUL byte: */
extern char *xmalloc_fgets(FILE *file) FAST_FUNC RETURNS_MALLOC;
/* Chops off '\n' from the end, unlike fgets: */
extern char *xmalloc_fgetline(FILE *file) FAST_FUNC RETURNS_MALLOC;
void *xzalloc(size_t size) FAST_FUNC RETURNS_MALLOC;
FILE* xfopen_stdin(const char *filename) FAST_FUNC;
void llist_add_to(llist_t **old_head, void *data) FAST_FUNC;
void llist_add_to_end(llist_t **list_head, void *data) FAST_FUNC;
void *llist_pop(llist_t **elm) FAST_FUNC;
void llist_unlink(llist_t **head, llist_t *elm) FAST_FUNC;
void llist_free(llist_t *elm, void (*freeit)(void *data)) FAST_FUNC;
llist_t *llist_rev(llist_t *list) FAST_FUNC;
llist_t *llist_find_str(llist_t *first, const char *str) FAST_FUNC;
int fclose_if_not_stdin(FILE *file) FAST_FUNC;
FILE* fopen_for_read(const char *path) FAST_FUNC;
extern int recursive_action(const char *fileName, unsigned flags,
    int FAST_FUNC (*fileAction)(const char *fileName, struct stat* statbuf, void* userData, int depth),
    int FAST_FUNC (*dirAction)(const char *fileName, struct stat* statbuf, void* userData, int depth),
    void* userData, unsigned depth) FAST_FUNC;
void *xrealloc(void *old, size_t size) FAST_FUNC;
void *xmalloc(size_t size) FAST_FUNC RETURNS_MALLOC;
char *xasprintf(const char *format, ...) __attribute__ ((format(printf, 1, 2))) FAST_FUNC RETURNS_MALLOC;
extern uint32_t option_mask32;
extern uint32_t getopt32(const char **argv, const char *applet_opts, ...) FAST_FUNC;
extern int grep_main(int argc, const char **argv);
void add_match_result(const char *file, int linenum, const char *line, long offset);

//#define ANDROID_JNI 1
#define DEBUGFMT  "\n##%s(%d):  %s %s"
#define DEBUGARGS __FILE__,__LINE__
#ifndef ANDROID_JNI
	#define EMSG(s)    printf((char *)(s))
	#define EMSG2(s, p)     printf(DEBUGFMT, __FILE__, __LINE__, (char *)(s), (char *)(p))
	#define LOGV(...) //printf(__VA_ARGS__)
#else
	#include <android/log.h>
	#define APPNAME "920TextEditorJNI"
	#define EMSG(s)    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, (s));
	#define EMSG2(s, p)     __android_log_print(ANDROID_LOG_DEBUG, APPNAME, DEBUGFMT, __FILE__, __LINE__, (char *)(s), (char *)(p))
	#define LOGV(...) __android_log_print(ANDROID_LOG_DEBUG, APPNAME, __VA_ARGS__)
#endif

#endif
