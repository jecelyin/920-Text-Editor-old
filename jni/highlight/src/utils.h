/*
 * utils.h
 *
 *  Created on: 2011-11-13
 *      Author: jecelyin
 */

#ifndef UTILS_H_
#define UTILS_H_

void _free(void *x);
char *skiptowhite(char *p);
int ends_excmd(int c);
char* skipwhite(char *q);
void _strncpy(char *to, char *from, size_t len);
void str_toupper(char *p);
char *strsave_up(char *string);
char *strsave(char *string);
char* str_tolower(char *str);
char *strnsave(char *string, int len);
char *malloc_clear(unsigned size);
char *strnsave_up(char *string, int len);

#endif /* UTILS_H_ */
