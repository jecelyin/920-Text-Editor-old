//==============================================================================
//      Section:        Includes 中文ok——
//==============================================================================
`include "Const.v"
//==============================================================================

//------------------------------------------------------------------------------
//      Module:         UAReceiver
//      Desc:           Standard Universal Asynchronous RS232/16550 type receiver.
//      Params:         ClockFreq: Frequency (in Hz) of the "Clock" being fed to this
//                                              module.
//                              Baud:   Desired Baud rate.  This is the rate at which this
//                                              module will send bits, and should be at most 1/4th
//                                              of the clock rate (or so).
//                              Width:  Word width (in bits) of the words (bytes) send over
//                                              the serial line.
//                              Parity: The type of parity bit to be appended to each word of
//                                              data.
//                                              0:      None
//                                              1:      Even
//                                              2:      Odd
//                                              3:      Mark
//                                              4:      Space
//                              StopBits:The number of bit-periods to send the stop condition.
//                                              Generally 1 or 2, though larger numbers are possible.
//      Ex:                     (27000000, 9600, 8, 0, 1) Standard 9600baud 8-N-1 serial port
//                                              settings used as the default by many devices, based
//                                              on a 27MHz clock.
//------------------------------------------------------------------------------
module UAReceiver(Clock, Reset, DataOut, DataOutValid, DataOutReady, SIn);
        //--------------------------------------------------------------------------
        //      Parameters
        //--------------------------------------------------------------------------
        parameter                               ClockFreq =                             27000000,
                                                        Baud =                                  115200,
                                                        Width =                                 8,
                                                        Parity =                                0,
                                                        StopBits =                              1;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Local Parameters
        //--------------------------------------------------------------------------
        `ifdef MACROSAFE
        localparam                              Divisor =                               ClockFreq / Baud,
                                                        DivWidth =                              `log2(Divisor),
                                                        Capture =                               (Divisor / 2),
                                                        BitCount =                              Width + StopBits + (Parity ? 1 : 0) + 1,
                                                        BCWidth =                               `log2(BitCount + 1),
                                                        ActualBaud =                    ClockFreq / Divisor;
        `endif

        `ifdef SIMULATION
        localparam real                 MaxBaud =                               ClockFreq / ((Divisor * (BitCount - 0.5)) / BitCount),
                                                        MinBaud =                               ClockFreq / ((Divisor * (BitCount + 0.5)) / BitCount);
        `endif
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Constant Debugging Statements
        //--------------------------------------------------------------------------
        `ifdef SIMULATION
                initial begin
                        $display("%m : UART");
                        $display("    ClockFreq =  %d", ClockFreq);
                        $display("    Baud =       %d", Baud);
                        $display("    Width =      %d", Width);
                        $display("    Parity =     %d", Parity);
                        $display("    StopBits =   %d", StopBits);

                        /*$display("    Divisor =    %d", Divisor);
                        $display("    DivWidth =   %d", DivWidth);
                        $display("    Capture =    %d", Capture);
                        $display("    BitCount =   %d", BitCount);
                        $display("    BCWidth =    %d", BCWidth);*/

                        $display("    ActualBaud = %d", ActualBaud);
                        $display("    MaxBaud =    %f", MaxBaud);
                        $display("    MinBaud =    %f", MinBaud);
                end
        `endif
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      System Inputs
        //--------------------------------------------------------------------------
        input                                   Clock, Reset;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Parallel Data Output
        //--------------------------------------------------------------------------
        output  [Width-1:0]             DataOut;
        output                                  DataOutValid;
        input                                   DataOutReady;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Serial Interface
        //--------------------------------------------------------------------------
        input                                   SIn;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //       Wires and Regs
        //--------------------------------------------------------------------------
        wire                                    IntSIn;

        wire    [DivWidth-1:0]  RxDivCount;
        wire    [BCWidth-1:0]   RxBitCount;
        wire    [BitCount-1:0]  RxData;
        wire                                    RxShiftEnable, RxRunning, RxBit, RxStart, RxTransfered;

        wire                                    RxStartBit, RxActualParity;
        wire    [StopBits-1:0]  RxStopBits;

        wire                                    RxParity;
        wire    [Width-1:0]             RxDataStripped;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Assigns and Decodes
        //--------------------------------------------------------------------------
        assign  RxShiftEnable =                                                 (RxDivCount == Capture);
        assign  RxRunning =                                                             (RxBitCount < BitCount);
        assign  RxBit =                                                                 RxRunning & RxShiftEnable;
        assign  RxStart =                                                               ~IntSIn & ~RxRunning;

        assign  RxStartBit =                                                    RxData[BitCount-1];
        assign  RxActualParity =                                                RxData[StopBits];
        assign  RxStopBits =                                                    RxData[StopBits-1:0];

        assign  DataOutValid =                                                  (~RxStartBit) & (&RxStopBits) & ~RxTransfered & (Parity ? ~(RxParity ^ RxActualParity) : 1'b1);
        assign  RxDataStripped =                                                Parity ? RxData[BitCount-2:StopBits+1] : RxData[BitCount-2:StopBits];
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      IO Register
        //--------------------------------------------------------------------------
        IORegister              IOR(            .Clock(                         Clock),
                                                                .Reset(                         1'b0),
                                                                .Set(                           1'b0),
                                                                .Enable(                        1'b1),
                                                                .In(                            SIn),
                                                                .Out(                           IntSIn));
        defparam                IOR.Width =                                             1;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Clock Divider Counter
        //--------------------------------------------------------------------------
        Counter                 RxDivCounter(.Clock(                    Clock),
                                                                .Reset(                         Reset | (RxDivCount == (Divisor-1)) | RxStart),
                                                                .Set(                           1'b0),
                                                                .Load(                          1'b0),
                                                                .Enable(                        1'b1),
                                                                .In(                            {DivWidth{1'bx}}),
                                                                .Count(                         RxDivCount));
        defparam                RxDivCounter.Width =                    DivWidth;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Bit Counter
        //--------------------------------------------------------------------------
        Counter                 RxBitCounter(.Clock(                    Clock),
                                                                .Reset(                         RxStart),
                                                                .Set(                           Reset),
                                                                .Load(                          1'b0),
                                                                .Enable(                        RxBit),
                                                                .In(                            {BCWidth{1'bx}}),
                                                                .Count(                         RxBitCount));
        defparam                RxBitCounter.Width =                    BCWidth;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Shift Register and Bit Reversal
        //--------------------------------------------------------------------------
        ShiftRegister   RxShift(        .PIn(                           {BitCount{1'b1}}),
                                                                .SIn(                           IntSIn),
                                                                .POut(                          RxData),
                                                                .SOut(                          ),
                                                                .Load(                          Reset),
                                                                .Enable(                        RxShiftEnable),
                                                                .Clock(                         Clock),
                                                                .Reset(                         1'b0));
        defparam                RxShift.PWidth =                                BitCount;
        defparam                RxShift.SWidth =                                1;
        Reverse                 RxReverse(      .In(                            RxDataStripped),
                                                                .Out(                           DataOut));
        defparam                RxReverse.Width =                               Width;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Register
        //--------------------------------------------------------------------------
        Register                RXTR(           .Clock(                         Clock),
                                                                .Reset(                         1'b0),
                                                                .Set(                           Reset | (DataOutReady & DataOutValid)),
                                                                .Enable(                        RxShiftEnable),
                                                                .In(                            (RxBitCount != (BitCount - 1))),
                                                                .Out(                           RxTransfered));
        defparam                RXTR.Width =                                    1;
        //--------------------------------------------------------------------------

        //--------------------------------------------------------------------------
        //      Parity Generator
        //--------------------------------------------------------------------------
        ParityGen               RxParityGen(.In(                                DataOut),
                                                                .Out(                           RxParity));
        defparam                RxParityGen.Width =                             Width;
        defparam                RxParityGen.Parity =                    Parity;
        //--------------------------------------------------------------------------
endmodule
//------------------------------------------------------------------------------
//end