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


int main(void) {
	int len=0;
	char *fileText;

	read_file2("/jecelyin/istudio/android/workspace/920-Text-Editor/build/test/file.php", &fileText);

	read_syntax("/jecelyin/istudio/android/workspace/920-Text-Editor/build/syntax/php.conf", (const char*)fileText, &len);
	//printf("%i \n",len);
	_free(fileText);

/*	read_file("/jecelyin/istudio/android/workspace_c/highlight/test/boot.php", &fileText);

	len=0;
	read_syntax("/jecelyin/istudio/android/workspace/920-Text-Editor/build/syntax/php.vim", (const char*)fileText, &len);
	//printf("%i \n",len);
	_free(fileText);*/

/*	char *str;
	str = malloc(200);
	memset(str, 0, 200);
	str = "测试abc";
	int len = 0;
	char *p;
	p = str;

	while(*p != NUL)
	{
		len = utfc_ptr2len(p);
		p += len;
		printf("%i", len);
	}*/

	return EXIT_SUCCESS;
}
