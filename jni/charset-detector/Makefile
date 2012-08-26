CXX = g++
CXXFLAGS = \
	-c -I./src/tables \
	-O2 -Wall \
	-fno-rtti -fno-exceptions -nostdinc++ \
	-D_REENTRANT
LDFLAGS =

SRCS = \
	src/CharDistribution.cpp \
	src/JpCntx.cpp \
	src/LangBulgarianModel.cpp \
	src/LangCyrillicModel.cpp \
	src/LangGreekModel.cpp \
	src/LangHebrewModel.cpp \
	src/LangHungarianModel.cpp \
	src/LangThaiModel.cpp \
	src/nsBig5Prober.cpp \
	src/nsCharSetProber.cpp \
	src/nsEUCJPProber.cpp \
	src/nsEUCKRProber.cpp \
	src/nsEUCTWProber.cpp \
	src/nsEscCharsetProber.cpp \
	src/nsEscSM.cpp \
	src/nsGB2312Prober.cpp \
	src/nsHebrewProber.cpp \
	src/nsLatin1Prober.cpp \
	src/nsMBCSGroupProber.cpp \
	src/nsMBCSSM.cpp \
	src/nsSBCSGroupProber.cpp \
	src/nsSBCharSetProber.cpp \
	src/nsSJISProber.cpp \
	src/nsUTF8Prober.cpp \
	src/nsUniversalDetector.cpp \
	abc.cpp

OBJS=$(SRCS:%.cpp=%.o)

all: $(OBJS)

$(OBJS): %.o: %.cpp
	libtool --mode=compile $(CXX) $(CXXFLAGS) $<

clean:
	rm -f *.o
	rm -f *.lo
	rm -rf .libs
