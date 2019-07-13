import itertools

import matplotlib.pyplot as plt
from datetime import date, timedelta
from matplotlib.rcsetup import cycler
from numpy import array, pi, exp, lcm, floor, log, sqrt
from typing import Tuple

NPOINTS = 10
RESFACT = 3
MAP = 'cool'
RUNS = 1.5


def highresLine(x1: int, x2: int, n: int) -> array:
    return [x1 + k / n * (x2 - x1) for k in range(0, n + 1)]


def highResPoints(x: array, y: array, factor=10) -> Tuple[array, array]:
    xmod = list(itertools.chain(
        *(highresLine(x[i], x[i+1], factor) for i in range(0, len(x) - 1))))
    ymod = list(itertools.chain(
        *(highresLine(y[i], y[i+1], factor) for i in range(0, len(y) - 1))))
    return xmod, ymod


def f(n: int, m: int, d: int, y: int) -> int:
    return n/m + n**2/d + n**3/y


def g(n: int, m: int, d: int, y: int) -> int:
    return log(n)**4.1 + n**2/(0.05/d)


def index(i: int, total: int) -> float:
    if RUNS*i/(total-1) < 1:
        return RUNS * i / (total - 1)
    else:
        return RUNS*i/(total-1) - floor(RUNS*i/(total-1))


def create_svg_d(d: int, m: int, y: int, date=True, ind=0) -> None:
    # N = lcm.reduce([d, m, y])
    N = 200

    # z = array([exp(2*pi*1j*f(n, d, m, y)) for n in range(0, N+1)])
    z = array([exp(2*pi*1j*sqrt(n)*sqrt(2)) for n in range(1, N+2)])
    z = z.cumsum()

    cm = plt.get_cmap(MAP)
    ax = plt.gca()

    xHiRes, yHiRes = highResPoints(z.real, z.imag, RESFACT)
    # xHiRes, yHiRes = z.real, z.imag
    npointsHiRes = len(xHiRes)

    # print(str(N) + ' ' + str(npointsHiRes))

    ax.set_prop_cycle(cycler(color=[cm(index(i, npointsHiRes))
                                    for i in range(npointsHiRes - 1)]))
    for i in range(npointsHiRes - 1):
        ax.plot(xHiRes[i], yHiRes[i], 'o')
    plt.axes().set_aspect(1)
    plt.axis('off')
    plt.axes().get_xaxis().set_visible(False)
    plt.axes().get_yaxis().set_visible(False)

    name = 'sum-'+str(d) + '-'+str(m) + '-'+str(y) + \
        '.svg' if date else 'sum_' + str(ind) + '.svg'

    plt.savefig(name, bbox_inches='tight',
                transparent=True, dpi=300)
    plt.show()
    plt.clf()


def advance_day(day: date) -> Tuple[int, int, int, date]:
    next = day + timedelta(days=1)
    d = next.day
    m = next.month
    y = next.year % 100
    return d, m, y, next


def create_svg(advande: int, pre: int) -> None:
    today = date.today()
    d, m, y = today.day, today.month, today.year % 100
    for i in range(pre):
        d, m, y, today = advance_day(today)
    for i in range(advande):
        print('creating sum for: ' + str(d) + "." + str(m) + "." + str(y))
        create_svg_d(d, m, y, date=True, ind=i)
        print("Created sum for: " + str(d) + "." + str(m) + "." + str(y))
        d, m, y, today = advance_day(today)


print('SUM_OF_TODAY')
create_svg(1, 0)
