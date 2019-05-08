import matplotlib.pyplot as plt
from matplotlib.rcsetup import cycler
import numpy as np
from numpy import array, pi, exp, log, lcm, floor
from datetime import date
import math
from typing import Tuple

NPOINTS = 10
RESFACT = 10
MAP = 'cool'
RUNS = 1.1


def highResPoints(x: array, y: array, factor=10) -> Tuple[array, array]:
    # r is the distance spanned between pairs of points
    r = [0]
    for i in range(1, len(x)):
        dx = x[i]-x[i-1]
        dy = y[i]-y[i-1]
        r.append(np.sqrt(dx*dx+dy*dy))
    r = np.array(r)

    # rtot is a cumulative sum of r, it's used to save time
    rtot = []
    for i in range(len(r)):
        rtot.append(r[0:i].sum())
    rtot.append(r.sum())

    dr = rtot[-1]/(NPOINTS*RESFACT-1)
    xmod = [x[0]]
    ymod = [y[0]]
    rPos = 0  # current point on walk along data
    rcount = 1
    while rPos < r.sum():
        x1, x2 = x[rcount-1], x[rcount]
        y1, y2 = y[rcount-1], y[rcount]
        dpos = rPos-rtot[rcount]
        theta = np.arctan2((x2-x1), (y2-y1))
        rx = np.sin(theta)*dpos+x1
        ry = np.cos(theta)*dpos+y1
        xmod.append(rx)
        ymod.append(ry)
        rPos += dr
        while rPos > rtot[rcount+1]:
            rPos = rtot[rcount+1]
            rcount += 1
            if rcount > rtot[-1]:
                break

    return xmod, ymod


def f(n: int, m: int, d: int, y: int) -> int:
    return n/m + n**2/d + n**3/y


def index(i: int, total: int) -> int:
    if RUNS*i/(total-1) < 1:
        return RUNS*i/(total-1)
    else:
        return RUNS*i/(total-1) - floor(RUNS*i/(total-1))


def create_svg_d(d: int, m: int, y: int, date=True, ind=0) -> None:
    N = lcm.reduce([d, m, y])

    z = array([exp(2*pi*1j*f(n, d, m, y)) for n in range(0, N+1)])
    z = z.cumsum()

    cm = plt.get_cmap(MAP)
    ax = plt.gca()

    xHiRes, yHiRes = highResPoints(z.real, z.imag, RESFACT)
    # xHiRes, yHiRes = z.real, z.imag
    npointsHiRes = len(xHiRes)

    ax.set_prop_cycle(cycler(color=[cm(index(i, npointsHiRes))
                                    for i in range(npointsHiRes - 1)]))
    for i in range(npointsHiRes - 1):
        ax.plot(xHiRes[i:i+2], yHiRes[i:i+2])

    plt.axes().set_aspect(1)
    plt.axis('off')
    plt.axes().get_xaxis().set_visible(False)
    plt.axes().get_yaxis().set_visible(False)

    name = 'sum-'+str(d) + '-'+str(m) + '-'+str(y) + \
        '.svg' if date else 'sum_' + str(ind) + '.svg'

    plt.savefig(name,
                bbox_inches='tight',
                transparent=True,
                dpi=300)
    plt.clf()


def advance_day(d: int, m: int, y: int) -> Tuple[int, int, int]:
    ml = 31 if m in [1, 3, 5, 7, 8, 10, 12] else 28 if m == 2 else 30
    d = d + 1
    if d % ml == 0:
        d = ml
    elif d % ml == 1:
        m = m + 1
        if m % 12 == 0:
            m = 12
        elif m % 12 == 1:
            y = (y + 1) % 100
    return d, m, y


def create_svg(advande: int) -> None:
    today = date.today()
    d = today.day
    m = today.month
    y = (today.year) % 100
    mont = date.month
    for i in range(advande):
        create_svg_d(d, m, y, date=True, ind=i)
        d, m, y = advance_day(d, m, y)


create_svg(10)
