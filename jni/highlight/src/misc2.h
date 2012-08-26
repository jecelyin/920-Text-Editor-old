/*
 * misc2.h
 *
 *  Created on: 2011-11-12
 *      Author: jecelyin
 */

#ifndef MISC2_H_
#define MISC2_H_

void ga_init(garray_T *gap);
void ga_init2(garray_T *gap,int itemsize,int growsize);
int ga_grow(garray_T *gap, int n);
void ga_clear(garray_T *gap);

#endif /* MISC2_H_ */
