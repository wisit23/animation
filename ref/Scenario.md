# Animation Scenario: My Memories

## 1. Project overview

This project is a 600 x 600 Java Swing animation created with the Java 2D API.
It presents a quiet present-day moment that triggers three childhood memories:
a funny football match, an imaginative toy-sword battle, and a happy day playing
in a forest stream. The animation uses a hand-drawn stickman style and loops
continuously every 22 seconds.

The visual story follows this structure:

```text
Present: looking at the night sky
        -> falls asleep
Memory 1: football match
        -> memory changes
Memory 2: toy-sword fight
        -> memory changes
Memory 3: playing in a forest stream
        -> wakes up in the present
```

## 2. Complete timeline

### Scene 1: Present - night stargazing (0.0-4.6 seconds)

The main character lies on a grassy hill at night. His head is on the left side
of the frame and his feet point to the right. Both hands are placed behind his
head, creating a relaxed pose. A contact shadow underneath the body helps the
character feel supported by the hill instead of floating above it.

The environment contains:

- a deep-blue vertical gradient sky;
- a diagonal Milky Way made from translucent curved bands;
- a large moon with glow, craters, and a crisp circular outline;
- many seeded stars with different sizes, colors, and twinkle phases;
- distant mountains and a dark grassy hill;
- swaying foreground grass and moonlit chamomile flowers;
- a dark vignette around the edges of the frame.

The character gently moves up and down to suggest breathing. From 1.2 to 2.6
seconds, a shooting star travels diagonally across the sky. From 1.8 to 4.3
seconds, the camera smoothly zooms toward the character's eye. The entire head
is rotated as one unit so the hair, eyes, mouth, and outline remain connected.

The eye animation begins at 3.2 seconds. The eyes gradually change from open to
closed and are fully closed at 3.9 seconds. This provides the visual motivation
for the transition into the memories.

At 4.6 seconds, the scene closes into a black transition before Memory 1 begins.
Expanding circular rings appear near the character's head during the warp.

### Scene 2: Memory 1 - football match (4.6-9.6 seconds)

The first memory takes place on a bright grassy football field. The scene shows
a player running beneath a falling ball and attempting an exaggerated bicycle kick.
A goalkeeper waits on the right side of the field.

The action develops as follows:

- At the beginning, the ball drops from above while the kicker runs into position.
- The player leaps and rotates backward into a bicycle-kick pose.
- At approximately 1.95 seconds into the memory, the foot contacts the ball.
- The ball flies quickly toward the goalkeeper with a visible motion trail.
- The goalkeeper is hit and is carried toward the right side of the screen.
- Speed lines, impact bursts, screen shake, and a short white impact flash emphasize
  the comedic hit.
- After the action, the kicker lands and performs a celebratory SIUUU pose.

At 9.6 seconds, expanding rings and a white warp transition move the animation
into the second memory.

### Scene 3: Memory 2 - toy-sword fight (9.6-15.6 seconds)

The second memory is an imaginative childhood battle in a dramatic sunset field.
The background contains a purple-to-orange gradient sky, a glowing sun, distant
mountains, layered hills, embers, and a dark foreground battlefield.

Two children face each other while holding glowing toy swords. One child uses a
blue scarf and cyan sword aura. The other uses a red scarf and red sword aura.

The action is divided into four phases:

1. **Energy charge (0.0-0.6 seconds):** both children hold battle-ready stances
   while colored energy pulses near their bodies.
2. **Dash (0.6-1.4 seconds):** both children rush toward the center with speed
   lines and animated running poses.
3. **Sword clash (1.4-3.0 seconds):** the children exchange rapid slashes. Curved
   slash ribbons, sparks, impact bursts, and screen shake make the toy battle feel
   intentionally exaggerated.
4. **BONK and aftermath (3.0-6.0 seconds):** one child lands a downward hit. The
   comic word “BONK!” appears during the impact. The injured child then cries with
   exaggerated waterfall tears while the other child panics and apologizes.

At 15.6 seconds, a white warp transition opens the stream memory.

### Scene 4: Memory 3 - playing in a forest stream (15.6-19.6 seconds)

The third memory is a warm and energetic childhood scene based on the reference
image in `ref/scene4/1.png`. Four friends are playing together in a shallow stream
inside a bright green forest.

The environment contains:

- a layered green forest canopy;
- several tree trunks and branches framing the left and right sides;
- dappled sunlight filtering through the leaves;
- a shallow blue-green stream occupying the lower half of the scene;
- curved current lines and bright water ripples;
- rocks along both stream banks and large foreground rocks;
- splashing water droplets and bright water highlights.

The four characters have different roles so the scene reads as an active group
memory instead of four identical standing figures:

- **Left friend - splashing:** leans toward the group and throws a stream of water
  toward the center. Droplets follow a curved arc from the hands.
- **Center-back friend - laughing:** stands slightly farther back, raises the arms,
  and can hold a simple bucket-like prop above the head.
- **Center-front friend - celebrating:** sits or kneels in the stream, laughs with
  an open smile, and raises both hands in excitement.
- **Right friend - jumping:** jumps from a rock into the stream with both feet lifted,
  arms raised, and a large splash at the landing area.

The three friends already in the water are drawn first. A translucent water-surface
layer is then drawn over their lower bodies so the waterline crosses around the
waist. The jumping friend is drawn in front of this layer so the airborne pose and
the splash remain visible.

The stream scene uses stronger foreground and background separation than the
earlier version. The forest and distant foliage stay behind the characters, while
rocks, ripples, and splashes appear in front of the water. This creates depth and
makes the characters look like they are inside the stream.

At 19.6 seconds, the stream memory closes with a black fade before returning to
the present scene.

### Scene 5: Return to the present (19.6-22.0 seconds)

The night stargazing scene returns with the camera still close to the character's
eye. From 19.6 to 20.2 seconds, the black return transition completes. From 20.2
to 21.6 seconds, the camera smoothly zooms back out to the original composition.

The character's eyes reopen from 20.8 to 21.4 seconds. The character is once again
lying peacefully on the grass. At 22.0 seconds, the animation loops back to 0.0
seconds and begins the stargazing scene again.

## 3. Transitions and visual effects

- The transition from the present into Memory 1 uses a black fade because it is
  connected to the character closing his eyes.
- The transition between Memory 1 and Memory 2 uses a white warp flash.
- The transition into Memory 3 uses expanding circular rings and a bright white
  warp to create a new memory reveal.
- The transition from Memory 3 back to the present uses a black fade.
- Warp rings originate near the character's head and are drawn with the midpoint
  circle algorithm.
- The football impact and sword “BONK!” moment use separate short white flashes and
  screen shake.
- Stars twinkle, grass moves in the wind, water ripples animate, and droplets move
  with the stream actions.

## 4. Camera and presentation

- The viewport is fixed at 600 x 600 pixels.
- The present scene uses a side-oriented lying pose and a targeted zoom toward the
  eye.
- The camera transforms the night scene as a complete composition instead of
  moving individual facial or body parts independently.
- The memory scenes use full-frame action compositions.
- The first and final transitions use a black fade; the internal memory reveals
  use white warp flashes and rings.
- The animation runs at approximately 60 frames per second and repeats automatically.

## 5. Java 2D and computer-graphics techniques

The program is implemented using Java Swing and Java 2D only. It demonstrates:

- Bresenham line drawing for stickman limbs and action lines;
- DDA line drawing from the graphics laboratory algorithms;
- Bezier curves for hair, faces, mouths, eyelids, grass, waves, and slash effects;
- midpoint circle drawing for the moon, heads, droplets, sparks, and warp rings;
- midpoint ellipse drawing for flowers, craters, petals, and small water details;
- `AffineTransform` for the present-scene camera zoom and head rotation;
- alpha blending and gradient paints for atmosphere, lighting, water, and transitions;
- elapsed-time animation using normalized progress and smooth-step interpolation.

## 6. Story purpose

The animation presents memories as emotionally connected snapshots. The quiet
night scene establishes the present. Closing the eyes becomes the gateway into
childhood memories. The three memories deliberately vary in mood:

- football provides comedy and energetic movement;
- the toy battle provides imagination and exaggerated drama;
- the stream scene provides friendship, laughter, and shared happiness.

Returning to the night scene completes the loop and suggests that the character is
remembering these moments while resting under the stars.
