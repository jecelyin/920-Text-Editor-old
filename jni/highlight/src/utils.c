/*
 * utils.c
 *
 *  Created on: 2011-11-13
 *      Author: jecelyin
 */

#include "global.h"

void _free(void *x)
{
	if (x != NULL)
	{
		free(x);
		x = NULL;
	}
}

char *skiptowhite(char *p)
{
	while (!ISWHITE(*p) && !ISBREAK(*p) && *p != NUL)
		++p;
	return p;
}

char* skipwhite(char *q)
{
	char *p = q;

	while (ISWHITE(*p)) /* skip to next non-white */
		++p;
	return p;
}

int ends_excmd(int c)
{
	return (c == NUL || c == '|' || c == '#' || c == '\n');
}

char *strsave(char *string)
{
	char *p;
	unsigned len;

	len = (unsigned) STRLEN(string) + 1;
	p = malloc(len);
	if (p != NULL)
		memmove(p, string, (size_t)len);
	return p;
}

char *strnsave(char *string, int len)
{
	char *p;

	p = malloc((unsigned) (len + 1));
	if (p != NULL)
	{
		STRNCPY(p, string, len);
		p[len] = NUL;
	}
	return p;
}

void _strncpy(char *to, char *from, size_t len)
{
	STRNCPY(to, from, len);
	to[len] = NUL;
}

void str_toupper(char *p)
{
	char *p2;
	int c;

	if (p != NULL)
	{
		p2 = p;
		while ((c = *p2) != NUL)
#ifdef EBCDIC
			*p2++ = isalpha(c) ? toupper(c) : c;
#else
			*p2++ = (c < 'a' || c > 'z') ? c : (c - 0x20);
#endif
	}
}

char* str_tolower(char *str)
{
	char *p;
	p = str;
	while (*str != NUL)
	{
		*str = tolower(*str);
		str++;
	}
	return p;
}

char *strsave_up(char *string)
{
	char *p1;

	p1 = strsave(string);
	str_toupper(p1);
	return p1;
}

/*
 * Like vim_strnsave(), but make all characters uppercase.
 * This uses ASCII lower-to-upper case translation, language independent.
 */
char *strnsave_up(char *string, int len)
{
    char *p1;

    p1 = strnsave(string, len);
    str_toupper(p1);
    return p1;
}

/*
 * Allocate memory and set all bytes to zero.
 */
char *malloc_clear(unsigned size)
{
	char *p;

	p = malloc((size_t) size);
	if (p != NULL)
		memset(p, 0, (size_t) size);
	return p;
}

