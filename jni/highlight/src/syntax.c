/**
 *   920 Text Editor is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   920 Text Editor is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */


#include "global.h"

struct g_buf
{
    int cur_offset; //用来保存当前偏移，
    int start_offset; //临时记录一个偏移开始的地方
    int syn_case; //Case sensitive
    hashtab_T b_keywtab; /* syntax keywords hash table */
    hashtab_T b_keywtab_ic; /* idem, ignore case */
    garray_T b_syn_patterns; /* table for syntax patterns */
};
typedef struct g_buf buf_T;
buf_T *curbuf = NULL;

struct keywordItem
{
    int group_id;
    char keyword[1];
};
typedef struct keywordItem keywordItem_T;

#define SPO_COUNT	7
//region/match命令属性值
#define ITEM_START	    0
#define ITEM_SKIP	    1
#define ITEM_END	    2
#define ITEM_TAG	    3
#define ITEM_END2	    4
#define ITEM_HG         5
#define ITEM_MATCH      6

//tag值列表
#define TAG_DEFAULT     NULL    //默认tag值，
#define TAG_SKIPEND     "skipend"    //跳过结束tag,如PHP的 '?>' tag
#define TAG_EXINC_END   "exinc_end"  //当前tag排除结束tag，如PHP的//范围到?>就结束了
#define TAG_KEEPEND     "keepend"    //必须存在结束tag,如asp：<%%> html:<>
//group id定义
#define GROUP_TAG   "Tag"
#define GROUP_COMMENT   "Comment"
#define GROUP_STRING   "String"
#define GROUP_KEYWORD   "Keyword"
#define GROUP_FUNCTION   "Function"
#define GROUP_ATTR_NAME   "AttrName"  //html标签的属性名
//全局变量
#define GROUP_TAG_ID         1
#define GROUP_COMMENT_ID     2
#define GROUP_STRING_ID      3
#define GROUP_KEYWORD_ID     4
#define GROUP_FUNCTION_ID    5
#define GROUP_ATTR_NAME_ID   6

//end tag values
#define MAXKEYWLEN      80   //关键字最大长度
#define MAX_TAG_NUM     10   //最大tag代码块标签的个数
#define MAX_SPLIT_MATCH_LENGTH     255   //含有|正则符号的正则最大字符长度
#define MAX_FORWARD_MATCH_LENGTH    80    //向前匹配\z匹配到的最大长度限制
/*
 * The patterns that are being searched for are stored in a syn_pattern.
 * A match item consists of one pattern.
 * A start/end item consists of n start patterns and m end patterns.
 * A start/skip/end item consists of n start patterns, one skip pattern and m
 * end patterns.
 * For the latter two, the patterns are always consecutive: start-skip-end.
 *
 * A character offset can be given for the matched text (_m_start and _m_end)
 * and for the actually highlighted text (_h_start and _h_end).
 */
typedef struct syn_pattern
{
    char *start_pattern;
    char *skip_pattern;
    char *end_pattern;
    char *end2_pattern;
    char *tag_pattern;
    char *match_pattern; //match与region正则不能同时并存
    char *hg; //高亮微调s+-NUM,e+-NUM
    int from_id; /* 组ID，如phpComment */
//int to_id;  /* 最终高亮id */
} synpat_T;
//高亮-------------
/*
 * Structure that stores information about a highlight group.
 * The ID of a highlight group is also called group ID.  It is the index in
 * the highlight_ga array PLUS ONE.
 */
struct hl_group
{
    char *sg_name; /* highlight group name */
    char *sg_name_u; /* uppercase of sg_name */
    int from_id; /* link to this highlight group ID */
    int to_id; /* 最终高亮id */
};

/*
 * For the current state we need to remember more than just the idx.
 * When si_m_endpos.lnum is 0, the items other than si_idx are unknown.
 * (The end positions have the column number of the next char)
 */
typedef struct state_item
{
    int idx; /* index of syntax pattern or KEYWORD_IDX */
    int group_id; /* highlight group ID for keywords */
    int offset_start;
    int offset_end;
} stateitem_T;

static garray_T highlight_ga; /* highlight groups for 'highlight' option */
#define HL_TABLE() ((struct hl_group *)((highlight_ga.ga_data)))
#define SYN_ITEMS(buf)	((synpat_T *)((buf)->b_syn_patterns.ga_data))
/* current stack of state_items */
static garray_T current_state =
{ 0, 0, 0, 0, NULL };
#define CUR_STATE(idx)	((stateitem_T *)(current_state.ga_data))[idx]
/*
 * In a hashtable item "hi_key" points to "keyword" in a keyentry.
 * This avoids adding a pointer to the hashtable item.
 * KE2HIKEY() converts a var pointer to a hashitem key pointer.
 * HIKEY2KE() converts a hashitem key pointer to a var pointer.
 * HI2KE() converts a hashitem pointer to a var pointer.
 */
static keywordItem_T dumkey;
#define KE2HIKEY(kp)  ((kp)->keyword)
#define HIKEY2KE(p)   ((keywordItem_T *)((p) - (dumkey.keyword - (char *)&dumkey)))
#define HI2KE(hi)      HIKEY2KE((hi)->hi_key)
//function声明------------------------------
static void syn_cmd_case(char *keyword);
static void syn_cmd_keyword(char *charP);
static char *get_group_name(char *arg, char **name_end);
static int syn_check_group(char *pp, int len, int autoAdd);
static int syn_name2id(char *name);
static int syn_add_group(char *name);
static void add_keyword(char *name, int group_id);
static void syn_cmd_region(char *charP);
static char *get_syn_pattern(char *arg, synpat_T *ci, int item);
char* scan_syntax(char *text, char *tagEnd);
static int push_current_state();
static int check_keyword_id(char *text, int *group_id);
int end_text(char *text, char *tagEnd);
void parse_file(char* fileText);
void echo(char *text, int start, int end);
static int match_region(char *text, char *skip, char *endMatch, char *memMatch, int *matchLen);
static int add_match_result(int group_id, int offset_start, int offset_end);
char *highlight_link(char *charP);
static int add_tag_group(char *name);
static int match_text(char **cmp1, char **cmp2, char *memMatch, int case_match);
static void clear_current_state();
static void clear_keyword_table(hashtab_T *ht);
static void syntax_clear(buf_T *buf);
static void free_highlight();
void load_syntax_conf(const char *confFile);
static char *syn_cmd_include(char *charP, const char*confFile);
static void syn_cmd_match(char *charP);
static int get_offset_end();

/**
 * 开始实现具体内容
 */
void syntax_init()
{

    clear_current_state();
    curbuf = malloc(sizeof(buf_T));
    curbuf->cur_offset = 0;
    curbuf->start_offset = 0;
    hash_init(&curbuf->b_keywtab);
    hash_init(&curbuf->b_keywtab_ic);
    ga_init(&curbuf->b_syn_patterns);
    syn_cmd_case("ignore");
    //强制设置ID
    highlight_ga.ga_len = GROUP_TAG_ID - 1;
    add_tag_group(GROUP_TAG);

    highlight_ga.ga_len = GROUP_COMMENT_ID - 1;
    add_tag_group(GROUP_COMMENT);

    highlight_ga.ga_len = GROUP_STRING_ID - 1;
    add_tag_group(GROUP_STRING);

    highlight_ga.ga_len = GROUP_KEYWORD_ID - 1;
    add_tag_group(GROUP_KEYWORD);

    highlight_ga.ga_len = GROUP_FUNCTION_ID - 1;
    add_tag_group(GROUP_FUNCTION);

    highlight_ga.ga_len = GROUP_ATTR_NAME_ID - 1;
    add_tag_group(GROUP_ATTR_NAME);
}

static int add_tag_group(char *name)
{
    int group_id;
    group_id = syn_add_group(name);
    if (group_id > 0)
    {
        HL_TABLE()[group_id - 1].from_id = group_id;
        HL_TABLE()[group_id - 1].to_id = group_id;
    }
    return group_id;
}

/**
 * @param: confFile 语法文件完整路径如 /sdcard/c.conf
 * @param: fileText 源代码内容
 * @param: retLen 接收返回int数组长度
 * @return int[] groupid, offset start, offset end,,,,
 */
int *read_syntax(const char *confFile, const char *fileText, int *retLen)
{
    if (*confFile == NUL)
    {
        EMSG("buffer is null");
        return NULL;
    }

    char *text;

    int len;

    len = STRLEN(fileText);
    text = malloc(len + 1);
    memset(text, 0, len + 1);
    memcpy(text, fileText, len);

    syntax_init();

    load_syntax_conf(confFile);

    parse_file(text);
    //_free(text);

    if (current_state.ga_len < 1)
    {
        EMSG("current_state.ga_len < 1");
        goto READ_SYNTAX_END;
    }

    int i;
    int idx;
    stateitem_T *cur_si;
    int to_id;

    /*	for (idx = highlight_ga.ga_len; --idx >= 0;)
     {
     printf("%i : %i : %s \n", HL_TABLE()[idx].from_id, HL_TABLE()[idx].to_id, HL_TABLE()[idx].sg_name);
     }*/

    int *ret;
    int index = 0;
    len = current_state.ga_len * 3;
    *retLen = len;
    //TODO:用完记得free
    ret = malloc(sizeof(int) * len);
    for (i = 0; i < current_state.ga_len; i++)
    {
        to_id = 0;
        cur_si = &CUR_STATE(i);
        //注意前几个为高亮类型
        for (idx = highlight_ga.ga_len; --idx >= 0;)
        {
            if (HL_TABLE()[idx].from_id == cur_si->group_id)
            {
                to_id = HL_TABLE()[idx].to_id;
                break;
            }
        }
        if (to_id == 0)
        {
            EMSG("to_id == 0");
            continue;
        }

        printf("%i -> ", to_id);
        echo(text, cur_si->offset_start, cur_si->offset_end);
        ret[index++] = to_id;
        ret[index++] = cur_si->offset_start;
        ret[index++] = cur_si->offset_end;
    }
    //end label
    READ_SYNTAX_END: _free(text);
    syntax_clear(curbuf);
    return ret;
}

void load_syntax_conf(const char *confFile)
{
    char *charP;
    char *buf;
    char *cmd;
    char *type;

    if (read_file(confFile, &buf) < 1)
    {
        EMSG2("can't read syntax config file", (char*)confFile);
        return;
    }
    charP = buf;

    while (*charP != NUL)
    {
        //跳过空白
        while (ISWHITE(*charP) || ISBREAK(*charP))
            charP++;
        if (*charP == NUL)
            break;
        //跳过注释
        if (*charP == '#')
        {
            charP = goto_next_line(charP);
            continue;
        }

        //获取第一个关键字
        charP = get_word(charP, &cmd);

        int isSYN;
        int isHiLink;
        int isInclude;

        isSYN = STRCMP(cmd, "syn") == 0;
        isHiLink = STRCMP(cmd,"HiLink") == 0;
        isInclude = STRCMP(cmd,"include") == 0;
        _free(cmd);

        if (!isSYN && !isHiLink && !isInclude)
        {
            charP = goto_next_line(charP);
            continue;
        }
        //syn命令
        if (isSYN)
        {
            charP = get_word(charP, &type);

            //切换是否区分大小写
            if (STRCMP(type, "case") == 0)
            {
                syn_cmd_case(charP);
            }
            else if (STRCMP(type, "keyword") == 0)
            { //如果是关键字
                syn_cmd_keyword(charP);
            }
            else if (STRCMP(type, "region") == 0)
            {
                syn_cmd_region(charP);
            }
            else if (STRCMP(type, "match") == 0)
            {
                syn_cmd_match(charP);
            }

            _free(type);
        }
        else if (isHiLink)
        {
            //HiLink命令
        	charP = highlight_link(charP);
        }
        else if (isInclude)
        {
        	charP = syn_cmd_include(charP, confFile);
        }

        //下一行处理
        charP = goto_next_line(charP);
    }
    _free(buf);
}

static char *syn_cmd_include(char *charP, const char*confFile)
{
    char *start = skipwhite(charP);
    char *p;
    char *includeSubfile;
    char *filename;
    char *end;
    int len;

    p = (char*) confFile;
    end = skiptowhite(start);
    filename = strnsave(start, end - start);
    //取得当前路径
    p += STRLEN(confFile);
    while (--p > confFile)
    {
        if (*(p - 1) == '/')
            break;
    }
    //分配include的文件完整路径
    len = (end - start) + (p - confFile) + 1;
    includeSubfile = malloc(len);
    memset(includeSubfile, 0, len);
    STRNCPY(includeSubfile, confFile, p-confFile);
    STRNCPY(includeSubfile+(p-confFile), filename, end-start);
    _free(filename);
    //加载新的语法文件
    load_syntax_conf((const char*)includeSubfile);
    _free(includeSubfile);
    return end;
}

void parse_file(char* fileText)
{
    char *text = skipwhite(fileText);
    synpat_T *spp;

    int idx;
    int ii;

    curbuf->cur_offset += (text - fileText);
    current_state.ga_itemsize = sizeof(stateitem_T);
    current_state.ga_growsize = 3;

    int num = 0;
    //如果存在语法标签范围，则先扫描标签开始的位置
    synpat_T *tag[MAX_TAG_NUM] = { 0 };
    int tag_toid[MAX_TAG_NUM] = { 0 };

    for (idx = 0; idx < curbuf->b_syn_patterns.ga_len; ++idx)
    {
        spp = &(SYN_ITEMS(curbuf)[idx]);
        if(spp->tag_pattern != NULL)
        {
            tag[num] = spp;
            for (ii = highlight_ga.ga_len; --ii >= 0;)
            {
                if (spp->from_id == HL_TABLE()[ii].from_id)
                {
                    tag_toid[num] = HL_TABLE()[ii].to_id;
                }
            }
            ++num;
        }
    }

    LOGV("tag num: %i", num);
    if (num > 0)
    {

        int inc = TRUE;
        int len;
        int epLen;
        int start_offset;
        int othercmp;

        char *splitMatch;
        splitMatch = malloc(sizeof(char) * MAX_SPLIT_MATCH_LENGTH);
        if(splitMatch == NULL)
        {
            EMSG("can't malloc split match memory!");
            return;
        }
        //扫描tagStart开始位置
        while (*text != NUL)
        {
            inc = TRUE;
            for (idx = 0; idx < num; ++idx)
            {
                //len = STRLEN(tag[idx]->start_pattern);
                //if (STRNCMP(tag[idx]->start_pattern, text, len) == 0)
                len = 0;
                //ignore case
                if(match_region(text, tag[idx]->start_pattern, splitMatch, NUL, &len) == TRUE)
                {
                    text += len;
                    start_offset = curbuf->cur_offset;
                    curbuf->cur_offset += len;
                    //tag开始部分
                    if(tag_toid[idx] == GROUP_TAG_ID)
                    {
                        add_match_result(tag_toid[idx], start_offset, curbuf->cur_offset);
                    }

                    epLen = STRLEN(tag[idx]->end_pattern);
                    othercmp = STRCMP(tag[idx]->tag_pattern, "other");
                    if(othercmp == 0)
                    {//处理像asp,html 的注释<!-- -->在非代码块中的情况
                        while(*text != NUL && STRNCMP(tag[idx]->end_pattern, text, epLen) != 0)
                        {
                            len = utfc_ptr2len(text);
                            text += len;
                            //一个中文字符长度为1,不然java处理会出错
                            curbuf->cur_offset++;
                        }
                    }else{
                        //扫描tag代码块
                        text = scan_syntax(text, tag[idx]->end_pattern);
                    }

                    //*text == NUL 匹配<!--后不找到-->的情况
                    //<style type="text/cs 处理因为 < 匹配到最后的情况
                    //curbuf->cur_offset-1是因为有些地方add_match_result后会cur_offset++
                    if (*text == NUL && othercmp == 0 && get_offset_end() != (curbuf->cur_offset-1))
                    {
                    	add_match_result(tag_toid[idx], start_offset, curbuf->cur_offset);
                    }else if (STRNCMP(tag[idx]->end_pattern, text, epLen) == 0)
                    {
                        if(tag_toid[idx] == GROUP_TAG_ID)
                        {
                            start_offset = curbuf->cur_offset;
                        }
                        add_match_result(tag_toid[idx], start_offset, curbuf->cur_offset + epLen);
                        curbuf->cur_offset += epLen;
                        text += epLen;
                    }
                    inc = FALSE;
                    break;
                }
            }
            if (inc == TRUE)
            {
                len = utfc_ptr2len(text);
                text += len;
                //一个中文字符长度为1,不然java处理会出错
                curbuf->cur_offset++;
            }
        }//endwhile
        _free(splitMatch);
        splitMatch = NULL;
    }
    else
    {
        //没有语法标签范围限制，直接扫描整个文件
        scan_syntax(text, NUL);
    }

}

void echo(char *text, int start, int end)
{
    int len = end - start;
    text += start;
    char *p;
    p = strnsave(text, len);
    //LOGV("%i - %i: %c", start, end, p);
    printf("%i - %i #######################################\n", start, end);
    puts(p);

}

/**
 * 扫描语法位置
 * @param: text文本开始处
 * @param: tagEnd语法标签结束标签
 * @param: offset当前偏移位置
 */
char* scan_syntax(char *text, char *tagEnd)
{
    LOGV("start scan_syntax...");
    if (end_text(text, tagEnd) == TRUE)
    {
        return text;
    }
    int do_keywords; //是否执行关键字匹配
    //int offset_start;
    //int offset_end;
    int group_id = 0; //group id
    char *textStart;

    synpat_T *spp;
    char *splitMatch;
    char *memMatch; //记忆配对空间，用于类似PHP的 Heredoc结构

    int idx;
    int len;
    char *start;
    char *skip;
    char *end;
    char *end2;
    char *tag;
    char *match;
    int os; //offset start
    int oe; //offset end
    char *hg; //highlight
    char *hg_sign; //start or end
    char *hg_plus; //+ or 1
    int hg_offset;
    int inc_end; //处理最后一行为注释的情况

    //匹配到的范围长度
    int matchLen = 0;
    //正则 | 符号需要的空间
    splitMatch = malloc_clear(sizeof(char) * MAX_SPLIT_MATCH_LENGTH);
    if (splitMatch == NULL)
        return text;
    //缓存\z向前匹配需要的空间
    memMatch = malloc_clear(sizeof(char) * MAX_FORWARD_MATCH_LENGTH);
    if (memMatch == NULL)
    {
        _free(splitMatch);
        return text;
    }

    do_keywords = curbuf->b_keywtab.ht_used > 0 || curbuf->b_keywtab_ic.ht_used > 0;

    //保存开始位置
    textStart = text;
    int loopEnd = FALSE;
    while (!loopEnd)
    {
        LABEL_START_WHILE:
        if(*text == NUL)
            break;
        //跳过空白
        if (*text == ' ' || *text == '\t' || *text == '\n' || *text == '\r')
        {
            text++;
            curbuf->cur_offset++;
            continue;
        }
        matchLen = 0;
        group_id = 0;
        //这里先保存，因为check_keyword_id会改变cur_offset
        curbuf->start_offset = curbuf->cur_offset;
        //看看是不是关键字
        if (do_keywords && ISKEYWORD(*text) && (matchLen=check_keyword_id(text, &group_id)) > 0)
        {
            if (group_id != 0)
            {
                text += matchLen;
                curbuf->cur_offset += matchLen;
                add_match_result(group_id, curbuf->start_offset, curbuf->cur_offset);
            }
            //不能进行下面的text++，因为keyword之后可能跟着重要的内容，如：tr"/a/b/c"
            continue;
        }
        else if (curbuf->b_syn_patterns.ga_len)
        {

            len = curbuf->b_syn_patterns.ga_len;

            //扫描region
            for (idx = 0; idx < len; idx++)
            {
                if (*text == NUL)
                    break;
                spp = &(SYN_ITEMS(curbuf)[idx]);
                start = spp->start_pattern;
                skip = spp->skip_pattern;
                end = spp->end_pattern;
                tag = spp->tag_pattern;
                match = spp->match_pattern;
                end2 = spp->end2_pattern;

                //这里需要处理tag="exinc_end"的情况，其它含有tag的region将pass,例如php的 //注释符不能注释//abc?>中的?>
                if (tag != NULL && STRCMP(tag, TAG_EXINC_END) != 0)
                {
                    continue;
                }
                matchLen = 0;

                //再看看是否匹配match命令
                if (match != NULL && match_region(text, match, splitMatch, memMatch, &matchLen) == TRUE)
                {
                    curbuf->start_offset = curbuf->cur_offset;
                    text += matchLen;
                    curbuf->cur_offset += matchLen;
                    add_match_result(spp->from_id, curbuf->start_offset, curbuf->cur_offset);
                    goto LABEL_START_WHILE;
                }
                //先判断是否匹配region命令
                else if (start != NULL && match_region(text, start, splitMatch, memMatch, &matchLen) == TRUE)
                {
                    //保存匹配到start属性时的位置
                    curbuf->start_offset = curbuf->cur_offset;
                    text += matchLen;
                    curbuf->cur_offset += matchLen;
                    inc_end = FALSE;
                    //找到了标签开始的地方，则尝试找到它结束的地方
                    while (*text != NUL)
                    {
                        matchLen = 0;
                        if (skip != NULL)
                        {
                            //需要跳过一些东东
                            if (match_region(text, skip, splitMatch, memMatch, &matchLen) == TRUE)
                            {
                                curbuf->cur_offset += matchLen;
                                text += matchLen;
                                continue;
                            }
                        }
                        if (match_region(text, end, splitMatch, memMatch, &matchLen) == TRUE)
                        {
                            text += matchLen;
                            curbuf->cur_offset += matchLen;
                            if (end2 == NULL)
                            {
                                os = curbuf->start_offset;
                                oe = curbuf->cur_offset;
                                if (spp->hg)
                                {
                                    hg = spp->hg;
                                    if (*hg == 's' || *hg == 'e')
                                    {
                                        hg_sign = hg;
                                        hg++;
                                        hg_plus = hg;
                                        hg++;
                                        //hg暂时只支持长度小于10
                                        hg_offset = (int)(*hg-'0');
                                        if (*hg_sign == 's')
                                        {
                                            if (*hg_plus == '+')
                                            {
                                                os += hg_offset;
                                            }
                                            else
                                            {
                                                os -= hg_offset;
                                            }
                                        }
                                        else
                                        {
                                            if (*hg_plus == '+')
                                            {
                                                oe += hg_offset;
                                            }
                                            else
                                            {
                                                oe -= hg_offset;
                                            }
                                        }
                                    }
                                }
                                add_match_result(spp->from_id, os, oe);
                                inc_end = TRUE;
                                //进行下一个字符的开始处判断
                                goto LABEL_START_WHILE;
                            }
                            else
                            {
                                end2 = NULL;
                                //继续while一次end
                                continue;
                            }
                        }
                        else if (tagEnd != NUL && STRNCMP(tagEnd, text, STRLEN(tagEnd)) == 0)
                        {
                            add_match_result(spp->from_id, curbuf->start_offset, curbuf->cur_offset);
                            goto LABEL_BREAK_PARSE;
                        }
                        matchLen = utfc_ptr2len(text);
                        text += matchLen;
                        curbuf->cur_offset++;
                    }//endwhile (*text != NUL)
                    //处理region匹配最后一行结束符$的问题，如最后一行为//注释
                    if(inc_end == FALSE)
                    {
                        add_match_result(spp->from_id, curbuf->start_offset, curbuf->cur_offset);
                    }else if(*text == NUL)
                    {//*text == NUL预防过长的注释导致高亮不正常的问题
                         add_match_result(spp->from_id, curbuf->start_offset, curbuf->cur_offset);
                         goto LABEL_BREAK_PARSE;
                    }
                } //endif (match_region(text, start, splitMatch, memMatch, &matchLen) == TRUE)

            } //endfor (idx = len; --idx >= 0;)

            //跳过一些字词
            if(ISKEYWORD(*text))
            {
                while(ISKEYWORD(*text))
                {
                    text++;
                    curbuf->cur_offset++;
                }
                goto LABEL_START_WHILE;
            }
        } // endif (curbuf->b_syn_patterns.ga_len)

        loopEnd = end_text(text, tagEnd);
        if (!loopEnd)
        {
            matchLen = utfc_ptr2len(text);
            //echo(text, 0, matchLen);
            text += matchLen;
            curbuf->cur_offset++;
        }
    } //end while

    LABEL_BREAK_PARSE:

    _free(splitMatch);
    _free(memMatch);

    return text;
}

static int get_offset_end()
{
    stateitem_T *cur_si;

    if(current_state.ga_len < 1)
    	return -1;
	cur_si = &CUR_STATE(current_state.ga_len - 1);

    return cur_si->offset_end;
}

static int add_match_result(int group_id, int offset_start, int offset_end)
{
    stateitem_T *cur_si;
    /*
     * Push the item in current_state stack;
     */
    //if (push_current_state(next_match_idx) == OK) {
    if (push_current_state() == TRUE)
    {
        /*
         * If it's a start-skip-end type that crosses lines, figure out how
         * much it continues in this line.  Otherwise just fill in the length.
         */
        cur_si = &CUR_STATE(current_state.ga_len - 1);
        cur_si->group_id = group_id;
        cur_si->offset_start = offset_start;
        cur_si->offset_end = offset_end;
        return TRUE;
    }
    return FALSE;
}

static int match_region(char *text, char *syn, char *matchMem, char *memMatch, int *matchLen)
{
    char *pattern;
    char *p;
    int endIndex;
    //分配一些空间给region的end属性，因为end属性可能有a|b正则
    //char *endMatch;
    //endMatch = (char*)malloc(sizeof(char)*20);
    //end属性的|正则个数
    int expNum;
    //处理|正则
    pattern = syn;
    expNum = 1;
    //正则表达式空间，用来存储正则“|”（或）的内容
    p = matchMem;
    while (*pattern != NUL)
    {
        if (*pattern == '|')
        {
            *p++ = NUL;
            pattern++;
            expNum++;
        }
        else
        {
            *p++ = *pattern++;
        }
    }
    *p = NUL;
    pattern = matchMem;
    int len = 0;
    int retLen = 0;
    int tmpLen = 0;
    char *cmp1; //正则表达式
    char *cmp2; //文本代码
    /*==============注意，目前只支持^,\z,|,$正则表达式=========================================*/
    for (endIndex = expNum; --endIndex >= 0; pattern += len + 1)
    {
        len = STRLEN(pattern);
        //是否到了结束的地方
        /*if(*pattern == '$')
         {
         if(*text == '\n')
         {
         *matchLen = len;
         return TRUE;
         }
         }else if(STRNCMP(pattern, text, len) == 0)
         {
         *matchLen = len;
         return TRUE;
         }*/
        cmp1 = pattern;
        cmp2 = text;
        retLen = 0;

        //处理start="<<<\z$" end="^\z;$"
        if (*cmp1 == '^')
        {
            if ((*(cmp2 - 1) == NUL || *(cmp2 - 1) == '\n'))
            {
                cmp1++;
                //有可能返回-1，因为有\s*正则
                //default ignoring case
                tmpLen = match_text(&cmp1, &cmp2, memMatch, 2);
                if (tmpLen == 0)
                    break;
                if(tmpLen > 0)
                    retLen += tmpLen;
            }
            else
            {
                break;
            }
        }
        //逐个判断是否相等
        while (*cmp1 != NUL)
        {
            if (*cmp1 == '$')
            {
                if (*cmp2 == '\n' || *cmp2 == NUL)
                {
                    //可以匹配 $ 正则
                    *matchLen = retLen;
                    return TRUE;
                }
                else
                {
                    break;
                }
            }
            else
            {
                //default ignoring case
                //有可能返回-1，因为有\s*正则
                tmpLen = match_text(&cmp1, &cmp2, memMatch, 2);
                if (tmpLen == 0)
                    break;
                if(tmpLen > 0)
                    retLen += tmpLen;
            }

        } //endwhile
          //如果能正确地执行过cmp1正则表达式，则说明成功地匹配了
        if (*cmp1 == NUL)
        {
            *matchLen = retLen;
            return TRUE;
        }

    } //endfor 循环各个|正则
    return FALSE;
}

/**
 * 返回匹配到文本的长度，一个中文字符长度为1
 * 返回-1为碰到\s*这样的正则
 */
static int match_text(char **a, char **b, char *memMatch, int case_match)
{
    char *cmp1;
    char *cmp2;
    cmp1 = *a;
    cmp2 = *b;
    int tmpLen;
    int retLen = 0;

    // \z 表示只能包含 字母、数字和下划线，并且不能用数字和下划线作为开头
    if (*cmp1 == '\\' && *(cmp1 + 1) == 'z' && ((*cmp2 >= 'a' && *cmp2 <= 'z') || (*cmp2 >= 'A' && *cmp2 <= 'Z')))
    {
        //如果前面已经存储有\z匹配到的单词，则再有\z的话，就当成配对的，使用清空记忆存储
        if (*memMatch != NUL)
        {
            tmpLen = STRLEN(memMatch);
            if (STRNCMP(memMatch, cmp2, tmpLen) == 0)
            {
                cmp1 += STRLEN("\\z");
                cmp2 += tmpLen;
                retLen += tmpLen;
                memset(memMatch, 0, tmpLen);
            }
            else
            {
                return 0;
            }
        }
        else
        {
            cmp1 += STRLEN("\\z");

            while (ISKEYWORD(*cmp2))
            {
                *memMatch = *cmp2;
                //注意这个变量不会改变父级函数的变量
                memMatch++;
                cmp2++;
                retLen++;
            }
        }
    }
    //支持\s符号
    else if (*cmp1 == '\\' && *(cmp1 + 1) == 's')
    {
        if (*(cmp1 + 2) == '*')
        {
            //cmp2测试是否有0个或多个空白字符
            cmp1 += 3;
            while (ISWHITE(*cmp2) || ISBREAK(*cmp2))
            {
                cmp2++;
                retLen++;
            }
            //\s*没有匹配到时也是正确的，不能退出匹配
            if(retLen == 0)
                retLen = -1;
        }
        else if (*(cmp1 + 2) == '+')
        {
            //至少需要一个空白字符
            int whitenum = 0;
            while (ISWHITE(*cmp2) || ISBREAK(*cmp2))
            {
                cmp2++;
                retLen++;
                whitenum++;
            }
            if (whitenum == 0)
            {
                return 0;
            }
            else
            {
                cmp1 += 3;
            }
        }
        else
        {
            //\s只匹配一个字符
            if (ISWHITE(*cmp2) || ISBREAK(*cmp2))
            {
                cmp1 += 2;
                retLen++;
                cmp2++;
            }
            else
            {
                return 0;
            }
        }
    }
    //支持\w符号
    else if (*cmp1 == '\\' && *(cmp1 + 1) == 'w')
    {
        if (*(cmp1 + 2) == '+')
        {
            //至少需要一个字符:[a-zA-Z_]
            int kw = 0;
            while (ISKEYWORD(*cmp2))
            {
                cmp2++;
                retLen++;
                kw++;
            }
            if (kw == 0)
            {
                return 0;
            }
            else
            {
                cmp1 += 3;
            }
        }
        else
        {
            //\w只匹配一个字符
            if (ISKEYWORD(*cmp2))
            {
                cmp1 += 2;
                retLen++;
                cmp2++;
            }
            else
            {
                return 0;
            }
        }
    }
    //支持\b符号, 匹配一个单词\w的边缘或非单词\W的边缘
    else if (*cmp1 == '\\' && *(cmp1 + 1) == 'b')
    {
        char_u *word;
        word = (char_u*) (cmp2 - 1);
        //如果当前字符是一个word
        if (ISKEYWORD(*word))
        {
            //但是下一个字符还是一个word就不匹配了
            if (ISKEYWORD(*cmp2))
            {
                return 0;
            }
            cmp1 += 2;
            //防止\b匹配到-module(data).中的(的情况，正常来说\b也不应该当作一个匹配结果集的
            //retLen++;
            cmp2++;
        }
        else
        {
            //如果是当前字符是\W，但是下一个字符也是\W
            if (!ISKEYWORD(*word))
            {
                return 0;
            }
            cmp1 += 2;
            //retLen++;
            cmp2++;
        }
    }
    else
    {
        //支持\$
        if(*cmp1 == '\\' && *(cmp1+1) == '$')
            cmp1++;
        //如果^后不是\z，则直接判断后面一个字符是否相等
        int match = 0;
        /*
         * Try twice:
         * 1. matching case
         * 2. ignoring case
         */
        if(case_match == 1)
        {
            match = (*cmp1 == *cmp2);
        }else{
            match = (tolower(*cmp1) == tolower(*cmp2));
        }

        if(match)
        {
            cmp1++;
            cmp2++;
            retLen++;
        }
        else
        {
            //不相等就执行下一个匹配
            return 0;
        }
    }
    *a = cmp1;
    *b = cmp2;
    return retLen;
}

int end_text(char *text, char *tagEnd)
{
    if (*text == NUL)
        return 1;
    if (tagEnd == NUL)
        return 0;
    return STRNCMP(tagEnd, text, STRLEN(tagEnd)) == 0;
}

static int check_keyword_id(char *text, int *group_id)
{
    char *textStart;
    int kwlen; //关键字长度
    char *keyword; //关键字
    int round; //temp variable
    hashtab_T *ht; //temp
    hashitem_T *hi; //temp
    keywordItem_T *kp;

    //扫描当前偏移的单词（关键字）
    textStart = text;
    while (ISKEYWORD(*text))
    {
        text++;
    }
    kwlen = text - textStart;
    if (kwlen > MAXKEYWLEN)
    {
        return 0;
    }
    //strncpy(keyword, textStart, kwlen);
    keyword = strnsave(textStart, kwlen);
    /*
     * Try twice:
     * 1. matching case
     * 2. ignoring case
     */
    for (round = 1; round <= 2; ++round)
    {
        ht = round == 1 ? &curbuf->b_keywtab : &curbuf->b_keywtab_ic;
        if (ht->ht_used == 0)
            continue;
        if (round == 2) /* ignore case */
            keyword = str_tolower(keyword);

        /*
         * Find keywords that match.  There can be several with different
         * attributes.
         * When current_next_list is non-zero accept only that group, otherwise:
         *  Accept a not-contained keyword at toplevel.
         *  Accept a keyword at other levels only if it is in the contains list.
         */
        hi = hash_find(ht, keyword);
        if (!HASHITEM_EMPTY(hi))
        {
            kp = HI2KE(hi);
            if (kp != NULL)
            {
                *group_id = kp->group_id;
                _free(keyword);
                return kwlen;
            }
        }
    }
    _free(keyword);
    return 0;
}

/*
 * Add a new state to the current state stack.
 * It is cleared and the index set to "idx".
 * Return FAIL if it's not possible (out of memory).
 */
static int push_current_state()
{
    if (ga_grow(&current_state, 1) == FALSE)
        return FALSE;
    memset(&CUR_STATE(current_state.ga_len), 0, sizeof(stateitem_T));
    //CUR_STATE(current_state.ga_len).si_idx = idx;
    ++current_state.ga_len;
    return TRUE;
}

/**
 * 移动指针到下一行开始处
 */
char* goto_next_line(char *charP)
{
    char *p = charP;
    if (*p == NUL)
        return p;
    while (*p != '\n' && *p != NUL)
        ++p;

    while (*p == '\n')
        ++p;

    return p;
}

/**
 * 获取一个关键词，并将指针指到下一个关键字开始处
 */
char* get_word(char *string, char **word)
{
    char *start;

    while (ISWHITE(*string))
    {
        string++;
    }
    if (ends_excmd(*string) || *string == NUL)
        return string;

    start = string;
    //char c[8]={0};
    while (ISKEYWORD(*string))
    {
        //sprintf(c,"%c",(char)*string);
        //strcat(word, c);
        //hello would!
        string++;
    }

    int len = string - start;
    *word = strnsave(start, len);
    /**word = (char*) malloc(sizeof(char) * len + 1);
     if (*word == NULL)
     return string;
     memset(*word, NUL, sizeof(char) * len + 1);
     STRNCPY(*word, start, len);*/
    //(*word)[sizeof(len)] = NUL;
    while (ISWHITE(*string))
    {
        string++;
    }

    return string;
}

static void syn_cmd_case(char *keyword)
{
    if (keyword == NULL)
        return;
    char *match = "match";
    char *ignore = "ignore";
    //int len = STRLEN(match);
    if (STRNCMP(match, keyword, STRLEN(match)) == 0)
        curbuf->syn_case = FALSE;
    else if (STRNCMP(ignore, keyword, STRLEN(ignore)) == 0)
        curbuf->syn_case = TRUE;
}

static void syn_cmd_keyword(char *charP)
{
    char *rest;
    char *group_name_end;
    char *keyword_copy;
    char *p;
    char *kw;
    int cnt;
    char *arg = charP;
    int syn_id;
    rest = get_group_name(arg, &group_name_end);
    if (rest != NULL)
    {
        syn_id = syn_check_group(arg, (int) (group_name_end - arg), TRUE);

        /* allocate a buffer, for removing the backslashes in the keyword */
        keyword_copy = malloc(STRLEN(rest) + 1);
        if (keyword_copy != NULL)
        {
            /*
             * The options given apply to ALL keywords, so all options must be
             * found before keywords can be created.
             * 1: collect the options and copy the keywords to keyword_copy.
             */
            cnt = 0;
            p = keyword_copy;
            for (; rest != NULL && !ends_excmd(*rest); rest = skipwhite(rest))
            {
                //rest = get_syn_options(rest, &syn_opt_arg);
                if (rest == NULL || ends_excmd(*rest))
                    break;
                /* Copy the keyword, removing backslashes, and add a NUL. */
                while (*rest != NUL && !ISWHITE(*rest) && !ISBREAK(*rest))
                {
                    if (*rest == '\\' && rest[1] != NUL)
                        ++rest;
                    *p++ = *rest++;
                }
                *p++ = NUL;
                ++cnt;
            }

            /* Adjust flags for use of ":syn include". */
            //syn_incl_toplevel(syn_id, &syn_opt_arg.flags);
            /*
             * 2: Add an entry for each keyword.
             */
            for (kw = keyword_copy; --cnt >= 0; kw += STRLEN(kw) + 1)
            {
                add_keyword(kw, syn_id);
            }

            _free(keyword_copy);

        }
    }
}

/**
 * syn keyword phpFunctions  preg_grep preg_match_all
 * 中的 phpFunctions
 */
static char *get_group_name(char *arg, char **name_end)
{
    char *rest;

    *name_end = skiptowhite(arg);
    rest = skipwhite(*name_end);

    /*
     * Check if there are enough arguments.  The first argument may be a
     * pattern, where '|' is allowed, so only check for NUL.
     */
    if (ends_excmd(*arg) || *rest == NUL)
        return NULL;
    return rest;
}

/**
 * syn keyword phpFunctions  preg_grep preg_match_all
 * 获取 phpFunctions 的ID，失败返回0
 */
static int syn_check_group(char *pp, int len, int autoAdd)
{
    int id;
    char *name;

    name = strnsave(pp, len);
    if (name == NULL)
        return 0;

    id = syn_name2id(name);
    if (id == 0 && autoAdd == TRUE) /* doesn't exist yet */
        id = syn_add_group(name);
    else
        _free(name);
    return id;
}

/*
 * Lookup a highlight group name and return it's ID.
 * If it is not found, 0 is returned.
 */
static int syn_name2id(char *name)
{
    int i;
    char name_u[200];

    /* Avoid using stricmp() too much, it's slow on some systems */
    /* Avoid alloc()/free(), these are slow too.  ID names over 200 chars
     * don't deserve to be found! */
    _strncpy(name_u, name, 199);
    str_toupper(name_u);
    for (i = highlight_ga.ga_len; --i >= 0;)
        if (HL_TABLE()[i].sg_name_u != NULL && STRCMP(name_u, HL_TABLE()[i].sg_name_u) == 0)
            break;
    return i + 1;
}

/*
 * Add new highlight group and return it's ID.
 * "name" must be an allocated string, it will be consumed.
 * Return 0 for failure.
 */
static int syn_add_group(char *name)
{
    char *p;

    /* Check that the name is ASCII letters, digits and underscore. */
    for (p = name; *p != NUL; ++p)
    {
        if (!ISKEYWORD(*p) && *p != '_')
        {
            /* This is an error, but since there previously was no check only
             * give a warning. */
            EMSG2("W18: Invalid character in group name", name);
            _free(name);
            return 0;
        }
    }

    /*
     * First call for this growarray: init growing array.
     */
    if (highlight_ga.ga_data == NULL)
    {
        highlight_ga.ga_itemsize = sizeof(struct hl_group);
        highlight_ga.ga_growsize = 10;
    }

    /*
     * Make room for at least one other syntax_highlight entry.
     */
    if (ga_grow(&highlight_ga, 1) == FALSE)
    {
        _free(name);
        return 0;
    }

    memset(&(HL_TABLE()[highlight_ga.ga_len]), 0, sizeof(struct hl_group));
    HL_TABLE()[highlight_ga.ga_len].sg_name = name;
    HL_TABLE()[highlight_ga.ga_len].sg_name_u = strsave_up(name);
    ++highlight_ga.ga_len;

    return highlight_ga.ga_len; /* ID is index plus one */
}

/*
 * Add a keyword to the list of keywords.
 */
static void add_keyword(char *name, int group_id)
{
    keywordItem_T *kw;
    hashtab_T *ht;
    hashitem_T *hi;
    char *name_ic;
    long_u hash;

    if (curbuf->syn_case)
        name_ic = str_tolower(name); //str_foldcase(name, (int) STRLEN(name), name_folded, MAXKEYWLEN + 1);
    else
        name_ic = name;

    //注意这里如果不分配空间，会造成keyword引用了charP的地址/内容，更改charP的东东
    kw = (keywordItem_T*) malloc(sizeof(keywordItem_T) + STRLEN(name_ic));

    //注意这里只保存一位字符
    STRCPY(kw->keyword, name_ic);
    kw->group_id = group_id;

    if (curbuf->syn_case)
        ht = &curbuf->b_keywtab_ic;
    else
        ht = &curbuf->b_keywtab;

    hash = hash_hash(kw->keyword);
    hi = hash_lookup(ht, kw->keyword, hash);
    if (HASHITEM_EMPTY(hi))
    {
        /* new keyword, add to hashtable 注意了，keyword传进去后，你就会看到完整的单词了，真是不好理解 */
        hash_add_item(ht, hi, kw->keyword, hash);
    }
    else
    {
        /* keyword already exists, prepend to list */
        //kp->ke_next = HI2KE(hi);
        hi->hi_key = kw->keyword; //KE2HIKEY(kp);
    }
}

/*
 * Handle "syn region {group-name} start={start}  [skip {skip}] end {end} [{options}]".
 * e.g. syn region   phpRegion start="<?php" end="?>" tag="skipend"
 * skipend: 是否可以忽略结束符
 */
static void syn_cmd_region(char *charP)
{
    char *arg = charP;
    char *group_name_end = NULL;
    char *rest; /* next arg, NULL on error */
    char *key_end;
    char *key = NULL;
    //char *p;
    int item;
    //保存当前行的region属性语法
    synpat_T *synp; /* pointer to syn_pattern */
    int pat_count = 0; /* nr of syn_patterns found */
    int from_id; //from group id
    //int matchgroup_id = 0;
    int not_enough = FALSE; /* not enough arguments */
    int success = FALSE;
    int idx;
    int size;
    //syn_opt_arg_T syn_opt_arg;

    /* Isolate the group name, check for validity */
    rest = get_group_name(arg, &group_name_end);

    size = sizeof(synpat_T);
    curbuf->b_syn_patterns.ga_itemsize = size;
    curbuf->b_syn_patterns.ga_growsize = 10;

    /*
     * Allocate room for a syn_pattern, and link it in the list of
     * syn_patterns for this item, at the start (because the list is
     * used from end to start).
     */
    synp = (synpat_T *) malloc(size);
    if (synp == NULL)
    {
        rest = NULL;
        return;
    }
    memset(synp, 0, size);
    /*
     * get the options, patterns and matchgroup.
     */
    while (rest != NULL && !ends_excmd(*rest))
    {
        while (ISWHITE(*rest))
            rest++;
        //记录start=x的开始位置
        key_end = rest;
        while (*key_end && !ISWHITE(*key_end) && *key_end != '=' && !ISBREAK(*key_end))
            ++key_end;
        //保存start,end,skip属性名称
        key = strnsave_up(rest, (int) (key_end - rest));
        if (key == NULL) /* out of memory */
        {
            rest = NULL;
            break;
        }
        if (STRCMP(key, "TAG") == 0)
            item = ITEM_TAG;
        else if (STRCMP(key, "START") == 0)
            item = ITEM_START;
        else if (STRCMP(key, "END") == 0)
            item = ITEM_END;
        else if (STRCMP(key, "END2") == 0)
            item = ITEM_END2;
        else if (STRCMP(key, "SKIP") == 0)
            item = ITEM_SKIP;
        else if (STRCMP(key, "HG") == 0)
            item = ITEM_HG;
        else
            break;

        _free(key);
        rest = skipwhite(key_end);
        if (*rest != '=')
        {
            rest = NULL;
            EMSG2("E398: Missing '=': %s", arg);
            break;
        }
        rest = skipwhite(rest + 1);
        if (*rest == NUL)
        {
            not_enough = TRUE;
            break;
        }

        /*
         * Get the syntax pattern and the following offset(s).
         */
        rest = get_syn_pattern(rest, synp, item);

        ++pat_count;

    } //endwhile (rest != NULL && !ends_excmd(*rest))

    if (not_enough)
        rest = NULL;

    /*
     * Must have a "start" and "end" pattern.
     */
    if (rest != NULL && (synp->start_pattern == NULL || synp->end_pattern == NULL))
    {
        not_enough = TRUE;
        rest = NULL;
    }

    if (rest != NULL)
    {
        if (ga_grow(&(curbuf->b_syn_patterns), pat_count) != FALSE && (from_id = syn_check_group(arg, (int) (group_name_end - arg), TRUE)) != 0)
        {
            /*
             * Store the start/skip/end in the syn_items list
             */
            idx = curbuf->b_syn_patterns.ga_len;

            SYN_ITEMS(curbuf)[idx] = *synp;
            SYN_ITEMS(curbuf)[idx].from_id = from_id;

            ++curbuf->b_syn_patterns.ga_len;
            ++idx;
            LOGV("sp: %s, ep: %s, ga_len: %i", synp->start_pattern, synp->end_pattern, curbuf->b_syn_patterns.ga_len);
            success = TRUE; /* don't free the progs and patterns now */
        }
    }
    //group_name_end只是指向了arg的一个地址，不是一个有效的内存空间，不能释放，系统会收拾它
    //_free(group_name_end);
    /*
     * Free the allocated memory.
     */
    _free(synp);

    if (!success)
    {
        if (not_enough)
            EMSG2("E399: Not enough arguments: syntax region", arg);
        else if (rest == NULL)
            EMSG2("e_invarg2: %s", arg);
    }
}

static void syn_cmd_match(char *charP)
{
    char *arg = charP;
    char *group_name_end = NULL;
    char *rest; /* next arg, NULL on error */

    //保存当前行的region属性语法
    synpat_T *synp; /* pointer to syn_pattern */
    int from_id; //from group id
    //int matchgroup_id = 0;
    int not_enough = FALSE; /* not enough arguments */
    int success = FALSE;
    int idx;
    int size;

    /* Isolate the group name, check for validity */
    rest = get_group_name(arg, &group_name_end);

    size = sizeof(synpat_T);
    curbuf->b_syn_patterns.ga_itemsize = size;
    curbuf->b_syn_patterns.ga_growsize = 10;

    /*
     * Allocate room for a syn_pattern, and link it in the list of
     * syn_patterns for this item, at the start (because the list is
     * used from end to start).
     */
    synp = (synpat_T *) malloc(size);
    if (synp == NULL)
    {
        rest = NULL;
        return;
    }
    memset(synp, 0, size);

    while (ISWHITE(*rest))
        rest++;

    /*
     * Get the syntax pattern and the following offset(s).
     */
    rest = get_syn_pattern(rest, synp, ITEM_MATCH);

    /*
     * Must have a "start" and "end" pattern.
     */
    if (rest != NULL && synp->match_pattern == NULL)
    {
        not_enough = TRUE;
        rest = NULL;
    }

    if (rest != NULL)
    {
        if (ga_grow(&(curbuf->b_syn_patterns), 1) != FALSE && (from_id = syn_check_group(arg, (int) (group_name_end - arg), TRUE)) != 0)
        {
            /*
             * Store the start/skip/end in the syn_items list
             */
            idx = curbuf->b_syn_patterns.ga_len;

            SYN_ITEMS(curbuf)[idx] = *synp;
            SYN_ITEMS(curbuf)[idx].from_id = from_id;

            ++curbuf->b_syn_patterns.ga_len;
            ++idx;
            LOGV("match: %s, ga_len: %i", synp->match_pattern, curbuf->b_syn_patterns.ga_len);
            success = TRUE; /* don't free the progs and patterns now */
        }
    }
    //group_name_end只是指向了arg的一个地址，不是一个有效的内存空间，不能释放，系统会收拾它
    //_free(group_name_end);
    /*
     * Free the allocated memory.
     */
    _free(synp);

    if (!success)
    {
        if (not_enough)
            EMSG2("E399: Not enough arguments: syntax region", arg);
        else if (rest == NULL)
            EMSG2("e_invarg2: %s", arg);
    }
}

/*
 * Get one pattern for a ":syntax match" or ":syntax region" command.
 * Stores the pattern and program in a synpat_T.
 * Returns a pointer to the next argument, or NULL in case of an error.
 */
static char *get_syn_pattern(char *arg, synpat_T *ci, int item)
{
    char *end;
    char delimiter;
    char *pattern;
    //int idx;
    //char *cpo_save;

    /* need at least three chars */
    if (arg == NULL || arg[1] == NUL || arg[2] == NUL)
        return NULL;

    delimiter = *arg;
    //start="//" 处理"分界符
    for (end = arg + 1; *end != delimiter && *end != NUL;)
    {
        ++end;
    }

    if (*end == NUL || *end != delimiter)
    {
        EMSG2("分界符没有配对：", arg);
        return NULL;
    }

    pattern = strnsave(arg + 1, (int) (end - arg - 1));
    if (pattern == NULL)
        return NULL;
    switch (item)
    {
    case ITEM_START:
        ci->start_pattern = pattern;
        break;
    case ITEM_SKIP:
        ci->skip_pattern = pattern;
        break;
    case ITEM_END:
        ci->end_pattern = pattern;
        break;
    case ITEM_END2:
        ci->end2_pattern = pattern;
        break;
    case ITEM_TAG:
        ci->tag_pattern = pattern;
        break;
    case ITEM_HG:
        ci->hg = pattern;
        break;
    case ITEM_MATCH:
        ci->match_pattern = pattern;
        break;
    default:
        EMSG("item is not in(start,skip,end,tag)");
        return NULL;
    }

    //跳过分界符
    ++end;
    if (!ends_excmd(*end) && !ISWHITE(*end))
    {
        EMSG2("E402: Garbage after pattern:", arg);
        return NULL;
    }
    return skipwhite(end);
}

char* highlight_link(char *charP)
{
    char *from_start = charP; //前面已经有跳过空白了，skipwhite(charP);
    char *from_end;
    char *to_start;
    char *to_end;
    int from_id;
    int to_id;

    from_end = skiptowhite(from_start);
    to_start = skipwhite(from_end);
    to_end = skiptowhite(to_start);

    if (ends_excmd(*from_start) || ends_excmd(*to_start))
    {
        EMSG2("E412: HiLink command not enough arguments", from_start);
        return charP;
    }

    from_id = syn_check_group(from_start, (int) (from_end - from_start), TRUE);
    if (STRNCMP(to_start, "NONE", 4) == 0)
        to_id = 0;
    else
        to_id = syn_check_group(to_start, (int) (to_end - to_start), FALSE);

    if (from_id > 0 && to_id > 0)
    {
        HL_TABLE()[from_id - 1].from_id = from_id;
        HL_TABLE()[from_id - 1].to_id = to_id;
    }

    return to_end;
}

/*
 * Cleanup the current_state stack.
 */
static void clear_current_state()
{
    ga_clear(&current_state);
}

/**
 * Clear a whole keyword table.
 */
static void clear_keyword_table(hashtab_T *ht)
{
    hashitem_T *hi;
    int todo;
    keywordItem_T *kp;

    todo = (int) ht->ht_used;
    for (hi = ht->ht_array; todo > 0; ++hi)
    {
        if (!HASHITEM_EMPTY(hi))
        {
            --todo;
            kp = HI2KE(hi);
            _free(kp);
        }
    }
    hash_clear(ht);
    hash_init(ht);
}

/*
 * Clear all syntax info for one buffer.
 */
static void syntax_clear(buf_T *buf)
{
    int i;

    /* free the keywords */
    clear_keyword_table(&buf->b_keywtab);
    clear_keyword_table(&buf->b_keywtab_ic);

    /* free the syntax patterns */
    for (i = buf->b_syn_patterns.ga_len; --i >= 0;)
    {
        _free(SYN_ITEMS(buf)[i].start_pattern);
        _free(SYN_ITEMS(buf)[i].skip_pattern);
        _free(SYN_ITEMS(buf)[i].end_pattern);
        _free(SYN_ITEMS(buf)[i].end2_pattern);
        _free(SYN_ITEMS(buf)[i].tag_pattern);
        _free(SYN_ITEMS(buf)[i].hg);
        _free(SYN_ITEMS(buf)[i].match_pattern);
        SYN_ITEMS(buf)[i].from_id = 0;
    }
    ga_clear(&buf->b_syn_patterns);

    _free(buf);
    /* free the stored states */
    clear_current_state();

    free_highlight();
}

static void free_highlight()
{
    int i;

    for (i = 0; i < highlight_ga.ga_len; ++i)
    {
        HL_TABLE()[i].from_id = 0;
        HL_TABLE()[i].to_id = 0;
        //前几个是高亮属性，不能free的
        if (i <= GROUP_ATTR_NAME_ID)
            continue;
        _free(HL_TABLE()[i].sg_name);
        _free(HL_TABLE()[i].sg_name_u);
    }
    ga_clear(&highlight_ga);
}
