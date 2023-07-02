# Five Lights

We did an escape room a few days ago (name redacted) and one of the
puzzles was a light switching puzzle. I've solved many of these kinds
of puzzles before, but this one just completely stumped us. By
"stumped" I mean "three adults who love puzzles sat there and played
with it for almost 30 minutes and couldn't solve it - and the DM for
the escape room couldn't figure it out either."

We failed the escape room and all the way home I was thinking about
writing a quick program to check.

I cleaned up the program a bit and made it more flexible to test
some intuitions I had. So here you go.

## The puzzle rules

There's a board with 5 lights. The lights are either "off" (X), "red" (R), or "green" (G).

Let's use the following notation to describe the states:

    G  (green light on)
    G  (green light on)
    G  (green light on)
    R  (red light on)
    X  (light is off)

For brevity, the above state shall be written:

    GGGRX

The board actually looks like:

    0  B   G
    1  B   G
    2  B   G
    3  B   R
    4  B   X

Where "B" is a button, and the number next to it is a label to identify the button.

Pushing a button causes the adjacent light and the lights above and below (wrapping) to
increment. The lights increment X -> R -> G -> X. We'll go into detail below.

    XXXXX -> push button 1 -> RRRXX

In the sections below (and the program) we'll write this as:

       01234
     : XXXXX
    1: RRRXX
    2: GGGXX

Another example:

       01234
     : XXXXX
    0: RRXXR
    0: GGXXG

And to push the last button:

       01234
     : XXXXX
    4: RXXRR
    4: GXXGG

And to have more fun with it:

       01234
     : XXXXX
    1: RRRXX
    2: RGGRX
    3: RGXGR

## The problem

The lights start out as XXXXX and the hint we got after a while was to have
the lights be all green (GGGGG).

Think about how you'd get there for a while. I'll wait.

## How to solve the problem

This is a graph search problem. I went with a simple breadth-first search using
a set to ensure we don't walk the same state twice.

There's some optimizations we could do like:

*  A*: order next points to explore by distance from the target.
*  Meet-in-the-middle: search from start to goal and from goal to start,
   checking to see if they ever reach the same state.

But it turns out none of these are necessary. For 5 lights there's so few
unique states you can reach (80) that it's trivial.

## So....

Yeah, it's not possible to get from XXXXX to GGGGG. Can't be done.

So we must have missed something else or the system must be broken. I can't
imagine that it's broken, so it was almost definitely us.

Though...we asked the DM running the show and he couldn't solve it.
His binder was missing instructions on that puzzle. He further said that
hardly anyone books that room.

So....yeah.

Time for a politely worded email.
