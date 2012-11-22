/* vi: set sw=4 ts=4: */
/*
 * Utility routines.
 *
 * Copyright (C) 1999-2004 by Erik Andersen <andersen@codepoet.org>
 * Copyright (C) 2006 Rob Landley
 * Copyright (C) 2006 Denys Vlasenko
 *
 * Licensed under GPLv2, see file LICENSE in this source tree.
 */

/* We need to have separate xfuncs.c and xfuncs_printf.c because
 * with current linkers, even with section garbage collection,
 * if *.o module references any of XXXprintf functions, you pull in
 * entire printf machinery. Even if you do not use the function
 * which uses XXXprintf.
 *
 * xfuncs.c contains functions (not necessarily xfuncs)
 * which do not pull in printf, directly or indirectly.
 * xfunc_printf.c contains those which do.
 */

#include "libbb.h"


/* All the functions starting with "x" call bb_error_msg_and_die() if they
 * fail, so callers never need to check for errors.  If it returned, it
 * succeeded. */

#ifndef DMALLOC
/* dmalloc provides variants of these that do abort() on failure.
 * Since dmalloc's prototypes overwrite the impls here as they are
 * included after these prototypes in libbb.h, all is well.
 */
// Warn if we can't allocate size bytes of memory.
void* FAST_FUNC malloc_or_warn(size_t size)
{
	void *ptr = malloc(size);
	if (ptr == NULL && size != 0){
		EMSG("malloc_or_warn: memory");
	    return NULL;//bb_error_msg(bb_msg_memory_exhausted);
	}
	return ptr;
}

// Die if we can't allocate size bytes of memory.
void* FAST_FUNC xmalloc(size_t size)
{
	void *ptr = malloc(size);
	if (ptr == NULL && size != 0){
		EMSG("xmalloc: memory");
	    return NULL;//bb_error_msg_and_die(bb_msg_memory_exhausted);
	}
	return ptr;
}

// Die if we can't resize previously allocated memory.  (This returns a pointer
// to the new memory, which may or may not be the same as the old memory.
// It'll copy the contents to a new chunk and free the old one if necessary.)
void* FAST_FUNC xrealloc(void *ptr, size_t size)
{
	ptr = realloc(ptr, size);
	if (ptr == NULL && size != 0){
		EMSG("xrealloc: memory");
	    return NULL;//bb_error_msg_and_die(bb_msg_memory_exhausted);
	}
	return ptr;
}
#endif /* DMALLOC */

// Die if we can't allocate and zero size bytes of memory.
void* FAST_FUNC xzalloc(size_t size)
{
	void *ptr = xmalloc(size);
	memset(ptr, 0, size);
	return ptr;
}

// Die with an error message if we can't malloc() enough space and do an
// sprintf() into that space.
char* FAST_FUNC xasprintf(const char *format, ...)
{
    va_list p;
    int r;
    char *string_ptr;

    va_start(p, format);
    r = vasprintf(&string_ptr, format, p);
    va_end(p);

    if (r < 0){
    	EMSG("xasprintf: memory");
        return NULL; //bb_error_msg_and_die(bb_msg_memory_exhausted);
    }
    return string_ptr;
}
