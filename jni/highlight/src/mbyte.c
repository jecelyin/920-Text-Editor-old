#include "global.h"

int utf_ptr2len(char_u *p);
int utfc_ptr2len2(char_u *p);

/*
 * Lookup table to quickly get the length in bytes of a UTF-8 character from
 * the first byte of a UTF-8 string.
 * Bytes which are illegal when used as the first byte have a 1.
 * The NUL byte has length 1.
 */
static char utf8len_tab[256] =
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
				2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6,
				1, 1, };

int utfc_ptr2len(char *p) {
	return utfc_ptr2len2((char_u *)p);
}

int utfc_ptr2len2(char_u *p) {
	int len;
	int b0 = *p;

	if (b0 == NUL)
		return 0;
	if (b0 < 0x80 && p[1] < 0x80) /* be quick for ASCII */
		return 1;

	/* Skip over first UTF-8 char, stopping at a NUL byte. */
	len = utf_ptr2len(p);

	/* Check for illegal byte. 检查非法字节。 */
	if (len == 1 && b0 >= 0x80)
		return 1;

	return len;
}

/*
 * Get the length of a UTF-8 byte sequence, not including any following
 * composing characters.
 * Returns 0 for "".
 * Returns 1 for an illegal byte sequence.
 */
int utf_ptr2len(char_u *p)
{
	int len;
	int i;

	if (*p == NUL)
		return 0;
	len = utf8len_tab[*p];
	for (i = 1; i < len; ++i)
		if ((p[i] & 0xc0) != 0x80)
			return 1;
	return len;
}
