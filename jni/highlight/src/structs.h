/*
 * structs.h
 *
 *  Created on: 2011-11-14
 *      Author: jecelyin
 */

#ifndef STRUCTS_H_
#define STRUCTS_H_

/**
 * 哈希列表===============================
 */
/* Initial size for a hashtable.  Our items are relatively small and growing
 * is expensive, thus use 16 as a start.  Must be a power of 2. */
#define HT_INIT_SIZE 16
/* Item for a hashtable.  "hi_key" can be one of three values:
 * NULL:	   Never been used
 * HI_KEY_REMOVED: Entry was removed
 * Otherwise:	   Used item, pointer to the actual key; this usually is
 *		   inside the item, subtract an offset to locate the item.
 *		   This reduces the size of hashitem by 1/3.
 */
typedef struct hashitem_S
{
	long_u hi_hash; /* cached hash number of hi_key */
	char *hi_key;
} hashitem_T;

typedef struct hashtable_S
{
	long_u ht_mask; /* mask used for hash value (nr of items in
	 * array is "ht_mask" + 1) */
	long_u ht_used; /* number of items used */
	long_u ht_filled; /* number of items used + removed */
	int ht_locked; /* counter for hash_lock() */
	int ht_error; /* when set growing failed, can't add more
	 items before growing works */
	hashitem_T *ht_array; /* points to the array, allocated when it's
	 not "ht_smallarray" */
	hashitem_T ht_smallarray[HT_INIT_SIZE]; /* initial array */
} hashtab_T;

typedef long_u hash_T; /* Type for hi_hash */

/* Magic number used for hashitem "hi_key" value indicating a deleted item.
 * Only the address is used. */
extern char hash_removed;

/* The address of "hash_removed" is used as a magic number for hi_key to
 * indicate a removed item. */
#define HI_KEY_REMOVED &hash_removed
#define HASHITEM_EMPTY(hi) ( (hi)->hi_key == NULL || (hi)->hi_key == &hash_removed )

//高亮------------------------------------------------------
/*
 * Structure used for growing arrays.
 * This is used to store information that only grows, is deleted all at
 * once, and needs to be accessed by index.  See ga_clear() and ga_grow().
 */
typedef struct growarray
{
    int	    ga_len;		    /* current number of items used */
    int	    ga_maxlen;		    /* maximum number of items possible */
    int	    ga_itemsize;	    /* sizeof(item) */
    int	    ga_growsize;	    /* number of items to grow each time */
    void    *ga_data;		    /* pointer to the first item */
} garray_T;

#endif /* STRUCTS_H_ */
