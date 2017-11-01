#include <jni.h>

extern "C"

#define addmod(x,y) (uint16_t)((x+y)%65536)
#define submod(x,y) (uint16_t)((x-y)%65536)


uint16_t R1,R2,R3,R4,R5,R6,R7,R8;
uint16_t k1,k2,k3,k4,k5,k6,k7,k8;
const uint8_t sbox[][16] = { {0x07, 0x0c, 0x0e, 0x09, 0x02, 0x01, 0x05, 0x0f, 0x0b, 0x06, 0x0d, 0x00, 0x04, 0x08, 0x0a, 0x03},
                             {0x04, 0x0a, 0x01, 0x06, 0x08, 0x0f, 0x07, 0x0c, 0x03, 0x00, 0x0e, 0x0d, 0x05, 0x09, 0x0b, 0x02},
                             {0x02, 0x0f, 0x0c, 0x01, 0x05, 0x06, 0x0a, 0x0d, 0x0e, 0x08, 0x03, 0x04, 0x00, 0x0b, 0x09, 0x07},
                             {0x0f, 0x04, 0x05, 0x08, 0x09, 0x07, 0x02, 0x01, 0x0a, 0x03, 0x00, 0x0e, 0x06, 0x0c, 0x0d, 0x0b} };

const uint8_t sboxinv[][16] = { {0x0b, 0x05, 0x04, 0x0f, 0x0c, 0x06, 0x09, 0x00, 0x0d, 0x03, 0x0e, 0x08, 0x01, 0x0a, 0x02, 0x07},
                                {0x09, 0x02, 0x0f, 0x08, 0x00, 0x0c, 0x03, 0x06, 0x04, 0x0d, 0x01, 0x0e, 0x07, 0x0b, 0x0a, 0x05},
                                {0x0c, 0x03, 0x00, 0x0a, 0x0b, 0x04, 0x05, 0x0f, 0x09, 0x0e, 0x06, 0x0d, 0x02, 0x07, 0x08, 0x01},
                                {0x0a, 0x07, 0x06, 0x09, 0x01, 0x02, 0x0c, 0x05, 0x03, 0x04, 0x08, 0x0f, 0x0d, 0x0e, 0x0b, 0x00} };

void tohex(char *name1,char *name2)
{
    //pt -> plain text   hexpt -> plain text in Hex
    FILE *fpin,*fpout;
    char c;
    fpout = fopen(name2,"w");
    fpin = fopen(name1,"r");
    fscanf(fpin,"%c",&c);
    while(!feof(fpin))
    {
        if(c != '\n')
            fprintf(fpout,"%x ",(unsigned char)c);
        fscanf(fpin,"%c",&c);
    }
    fclose(fpin);
    fclose(fpout);
}

uint16_t lshift(uint16_t num,int n)
{
    uint32_t temp;
    temp = num;
    temp = temp << n;
    uint32_t x = temp & 0xffff0000;
    x = x>>16;
    temp = temp+x;
    num = temp & 0x0000ffff;
    return num;
}
uint16_t rshift(uint16_t num,int n)
{
    uint32_t temp;
    temp = num<<16;
    temp = temp >> n;
    uint32_t x = temp & 0x0000ffff;
    x = x<<16;
    temp = temp & 0xffff0000;
    temp = temp+x;
    temp = temp >>16;
    num = temp;
    return num;
}

uint16_t substitute(uint16_t pt)
{
    uint16_t subpt;
    uint16_t s0,s1,s2,s3;
    s0 = (pt & 0xf000)>>12;
    s1 = (pt & 0x0f00)>>8;
    s2 = (pt & 0x00f0)>>4;
    s3 = pt & 0x000f;
    // printf("%x %x %x %x           ",s0,s1,s2,s3);
    s0=sbox[0][s0];
    s1=sbox[1][s1];
    s2=sbox[2][s2];
    s3=sbox[3][s3];
    subpt = (s0<<12)+(s1<<8)+(s2<<4)+s3;
    return subpt;
    //printf("%x  ",subpt);
}
uint16_t L(uint16_t x)//MAKE CIRCULAR SHIFT
{
    x = (x) ^ (lshift(x,6)) ^ (lshift(x,10));
    return x;
}
uint16_t Fn(uint16_t x)
{
    return(L(substitute(x)));
}

uint16_t WD16(uint16_t x,uint16_t a,uint16_t b, uint16_t c,uint16_t d)
{
    x = Fn(Fn(Fn(Fn(x ^ a)^ b)^ c)^ d);
    return x;
}

void initialise(char hexIV[],char hexkey[])
{
    uint16_t IV1;
    uint16_t t1,t2,t3,t4;
    uint16_t i;
    uint16_t IV2;
    uint16_t x;
    uint16_t IV3;
    FILE *fin1;
    uint16_t IV4;
    fin1 = fopen(hexIV,"r");

    fscanf(fin1,"%"SCNx16,&IV1);
    fscanf(fin1,"%"SCNx16,&x);
    x = x << 8;
    IV1 = IV1 | x;
    // printf("IV1 : %x    ",IV1);
    fscanf(fin1,"%"SCNx16,&IV2);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    IV2 = IV2 | x;

    fscanf(fin1,"%"SCNx16,&IV3);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    IV3 = IV3 | x;

    fscanf(fin1,"%"SCNx16,&IV4);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    IV4 = IV4 | x;
    fclose(fin1);
    //printf("%x %x %x %x   \n",IV1,IV2,IV3,IV4);
    R1 = IV1;
    R2 = IV2;
    R3 = IV3;
    R4 = IV4;
    R5 = IV1;
    R6 = IV2;
    R7 = IV3;
    R8 = IV4;
    fin1 = fopen(hexkey,"r");
    fscanf(fin1,"%"SCNx16,&k1);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k1 = k1 | x;
    //printf("%x    " ,k1);
    fscanf(fin1,"%"SCNx16,&k2);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k2 = k2 | x;

    fscanf(fin1,"%"SCNx16,&k3);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k3 = k3 | x;

    fscanf(fin1,"%"SCNx16,&k4);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k4 = k4 | x;

    fscanf(fin1,"%"SCNx16,&k5);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k5 = k5 | x;

    fscanf(fin1,"%"SCNx16,&k6);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k6 = k6 | x;

    fscanf(fin1,"%"SCNx16,&k7);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k7 = k7 | x;

    fscanf(fin1,"%"SCNx16,&k8);
    fscanf(fin1,"%"SCNx16,&x);
    x = x<<8;
    k8 = k8 | x;
    fclose(fin1);

    for( i = 0; i < 4; i++)
    {
        t1 = WD16(addmod(R1,i),k1,k2,k3,k4);
        //printf("%x",t1);
        t2 = WD16(addmod(R2,t1),k5,k6,k7,k8);
        t3 = WD16(addmod(R3,t2),k1,k2,k3,k4);
        t4 = WD16(addmod(R4,t3),k5,k6,k7,k8);
        R1 = lshift((addmod(R1,t4)),3);
        R2 = rshift((addmod(R2,t1)),1);
        R3 = lshift((addmod(R3,t2)),8);
        R4 = lshift((addmod(R4,t3)),1);
        R5 = R5 ^ R1;
        R6 = R6 ^ R2;
        R7 = R7 ^ R3;
        R8 = R8 ^ R4;
    }
}

JNIEXPORT jstring JNICALL
Java_com_example_rkgoswami_otpencryption_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject jObj, jstring jStr) {

    const char *str1 = (*env).GetStringUTFChars(jStr,0);
    //return env->NewStringUTF(hello.c_str());
    return (*env).NewStringUTF(str1);
}
