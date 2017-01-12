using System;

public class Well512
{
    const double FACT = 2.32830643653869628906e-10;
    //static uint[] state = new uint[16];
    static uint[] state = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    
    static uint index = 0;
    
    static Well512()
    {
        //state 배열은 랜덤값에 대한 일치를 목적으로 사용하는 것이기 때문에 개발자가 직접 초기화 해줘야함.
        Random rand = new Random((int)DateTime.Now.Ticks);
        for (int i = 0; i < 16; i++)
            state[i] = (uint)rand.Next();
    }
    

    internal static uint Next(int minValue, int maxValue)
    {
        return (uint)((Next() % (maxValue - minValue)) + minValue);
    }

    public static uint Next(uint maxValue)
    {
        return Next() % maxValue;
    }

    public static uint Next()
    {
        uint a, b, c, d;

        a = state[index];
        c = state[(index + 13) & 15];
        b = a ^ c ^ (a << 16) ^ (c << 15);
        c = state[(index + 9) & 15];
        c ^= (c >> 11);
        a = state[index] = b ^ c;
        d = a ^ ((a << 5) & 0xda442d24U);
        index = (index + 15) & 15;
        a = state[index];
        state[index] = a ^ b ^ d ^ (a << 2) ^ (b << 18) ^ (c << 28);

        return state[index];
    }

    public static double DoubleNext()
    {
        uint a, b, c, d;

        a = state[index];
        c = state[(index + 13) & 15];
        b = a ^ c ^ (a << 16) ^ (c << 15);
        c = state[(index + 9) & 15];
        c ^= (c >> 11);
        a = state[index] = b ^ c;
        d = a ^ ((a << 5) & 0xda442d24U);
        index = (index + 15) & 15;
        a = state[index];
        state[index] = a ^ b ^ d ^ (a << 2) ^ (b << 18) ^ (c << 28);

        return (double)state[index] * FACT;
    }
}

