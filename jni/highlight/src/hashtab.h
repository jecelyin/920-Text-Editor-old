/*
 * hashtab.h
 *
 *  Created on: 2011-11-11
 *      Author: jecelyin
 */

#ifndef HASHTAB_H_
#define HASHTAB_H_

hash_T hash_hash(char *key);
hashitem_T *hash_lookup(hashtab_T *ht, char *key, hash_T hash);
int hash_add_item(hashtab_T *ht, hashitem_T *hi, char *key, hash_T hash);
void hash_init(hashtab_T *ht);
hashitem_T * hash_find(hashtab_T *ht, char *key);
void hash_clear(hashtab_T *ht);

#endif /* HASHTAB_H_ */
