# Design Principles

This document is a work-in-progress. New principles will be added and existing
ones refined. These principles will be used to motivate specific designs.


## Gain Leverage Through Tooling

Outside of pixel art and retro polygon modeling, graphic designers do not
loving place each and every individual pixel; modelers do not tweak every
vertex by hand. And even if they did, they sure as hell wouldn't type in RGB
hex codes or enter precise coordinate values. No, they'd use a graphical tool
with direct manipulation.

When faced with a tradeoff between convenient syntax for programmers and
uniformity that can be leveraged by tools, err in favor of the latter. When
the pain of inconvenient syntax and tiresome symbolic tweaking becomes too
great to bear, prefer automation over indirection.


## Default To Concrete

> "This idea that there is generality in the
> specific is of far-reaching importance."
>                  -- Douglas R. Hofstadter

As functional ideals gain traction in graphical user interface programming,
lambda begins to drown out all other mechanisms of abstraction. Functions
acheive abstraction through a priori parameterization and renders the
abstracted expression inert in the absence of those parameters. Components
described by template functions are flexible, but they are unforgiving. If
supplied incomplete or incorrect parameters, the template will fail.

By contrast, a specific working component can be instanced and modified to
yield a new working component. Each incremental modification preserves the
integrety of the composition, such that development of components can proceed
independently of their consumers. This property is critical for collaboration
with designers, effective tooling, and exploratory implementation. Therefore,
we prefer to abstract via prototypes, rather than templates.


## Write Once, Tune Everywhere

Even if "Write once, run everywhere" worked, it wouldn't be desirable for user
interfaces. Consistency with other applications on a platform is more
important than consistency of a single application between platforms.

As an alternative, "Learn once, write anywhere" leverages perspective and
tooling between platforms, but abandons the challenge of leveraging code reuse.

Instead, aim for the ideal of "Write once, tune anywhere", where you can reuse
an adjustable range of production code and assets between platforms. To achieve
this, the tooling must both expose and isolate host platform semantics.
Abstractions that span platforms must be provided out of the box and they
should achieve platform specialization in the same manner as components
are abstracted for reuse within a particular platform.
