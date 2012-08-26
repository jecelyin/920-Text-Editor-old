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
#include <limits.h> /* PATH_MAX定义 */
#include <fcntl.h>
#include <sys/stat.h>
/**
 * 返回文件大小
 */
int read_file(const char *file, char **buffer) {
	return read_file2((char*) file, buffer);
}

int read_file2(char *file, char **buffer) {
	FILE *fh;
	char absfile[PATH_MAX];
	char *f = NULL;

	f = realpath(file, absfile);
	if (f == NULL) {
		return 0;
	}
	fh = fopen(f, "rb");
	if (fh == NULL) {
		return 0;
	}
	fseek(fh, 0L, SEEK_END);
	long size = ftell(fh);
	//Sets the position indicator associated with stream to the beginning of the file.
	rewind(fh);
	//传说要多分配1字节，字符串都是以\0结束的
	int len = sizeof(char) * size + 1;
	char *buf;
	buf = malloc(len);
	if (buf == NULL) {
		LOGV("ERROR: Unable to allocate data buffer\n");
		fclose(fh);
		return 0;
	}
	memset(buf, 0, len);
	size_t bytes = fread(buf, 1, size, fh);
	if (bytes != size) {
		LOGV("ERROR: Incorrect size\n");
		_free(buf);
		size = 0;
	}

	// we can now close the file
	fclose(fh);
	fh = NULL;

	*buffer = buf;
	return size;

}

char *read_file3(const char *fn, int *_sz) {
	char *data;
	int sz;
	int fd;

	char absfile[PATH_MAX];
	char *f = NULL;

	f = realpath(fn, absfile);
	if (f == NULL) {
		return NULL;
	}

	data = NULL;
	fd = open(f, O_RDONLY);

	if (fd < 0)
		return NULL;

	sz = lseek(fd, 0, SEEK_END);

	if (sz < 0)
		goto oops;

	if (lseek(fd, 0, SEEK_SET) != 0)
		goto oops;

	data = (char*) malloc(sz + 2);
	if (data == NULL)
		goto oops;

	if (read(fd, data, sz) != sz)
		goto oops;
	close(fd);
	data[sz] = '\n';
	data[sz + 1] = 0;
	*_sz = sz;
	return data;

oops:
	close(fd);
	if (data != NULL) {
		free(data);
	}
	return NULL;
}
