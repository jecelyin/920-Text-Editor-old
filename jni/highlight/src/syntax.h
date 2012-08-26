/*
 * syntax.h
 *
 *  Created on: 2011-11-13
 *      Author: jecelyin
 */

#ifndef SYNTAX_H_
#define SYNTAX_H_


int *read_syntax(const char *confFile, const char *fileText, int *retLen);
char *goto_next_line(char *charP);
char *get_word(char *string, char **word);
void syntax_init();


#endif /* SYNTAX_H_ */
