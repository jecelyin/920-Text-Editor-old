/*
 * misc2.c
 *
 *  Created on: 2011-11-12
 *      Author: jecelyin
 */

#include "global.h"

/*
 * Initialize a growing array.	Don't forget to set ga_itemsize and
 * ga_growsize!  Or use ga_init2().
 */
void ga_init(garray_T *gap)
{
	gap->ga_data = NULL;
	gap->ga_maxlen = 0;
	gap->ga_len = 0;
}

void ga_init2(garray_T *gap, int itemsize, int growsize)
{
	ga_init(gap);
	gap->ga_itemsize = itemsize;
	gap->ga_growsize = growsize;
}

/*
 * Make room in growing array "gap" for at least "n" items.
 * Return FAIL for failure, OK otherwise.
 */
int ga_grow(garray_T *gap, int n)

{
	size_t len;
	char *pp;

	if (gap->ga_maxlen - gap->ga_len < n)
	{
		if (n < gap->ga_growsize)
			n = gap->ga_growsize;
		len = gap->ga_itemsize * (gap->ga_len + n);
		pp = malloc_clear((unsigned) len);
		if (pp == NULL)
			return FALSE;
		gap->ga_maxlen = gap->ga_len + n;
		if (gap->ga_data != NULL)
		{
			memmove(pp, gap->ga_data, (size_t)(gap->ga_itemsize * gap->ga_len));
			_free(gap->ga_data);
		}
		gap->ga_data = pp;
	}
	return TRUE;
}

/**
 * Clear an allocated growing array.
 */
void ga_clear(garray_T *gap)
{
    _free(gap->ga_data);
    ga_init(gap);
}
