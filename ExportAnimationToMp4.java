import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ExportAnimationToMp4 {
    // Video output settings.
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int FPS = 60;
    private static final String DEFAULT_OUTPUT = "Assignment1_67050522_67050637.mp4";

    private ExportAnimationToMp4() {
    }

    public static void main(String[] args) throws Exception {
        // Use the provided file name or the default MP4 name.
        Path output = Paths.get(args.length > 0 ? args[0] : DEFAULT_OUTPUT)
                .toAbsolutePath()
                .normalize();

        Assignment1_studentID_yourPairID animation = createAnimationPanel();
        Process ffmpeg = startFfmpeg(output);

        try (OutputStream videoInput = ffmpeg.getOutputStream()) {
            renderFrames(animation, videoInput);
        }

        int exitCode = ffmpeg.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg failed with exit code " + exitCode);
        }

        System.out.println("MP4 created: " + output);
    }

    private static Assignment1_studentID_yourPairID createAnimationPanel() {
        // Create an off-screen panel without starting the real-time loop.
        Assignment1_studentID_yourPairID animation = new Assignment1_studentID_yourPairID();
        animation.setSize(WIDTH, HEIGHT);
        animation.setDoubleBuffered(false);
        return animation;
    }

    private static Process startFfmpeg(Path output) throws IOException {
        // Start FFmpeg and send raw frames through standard input.
        String executable = findFfmpeg();
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("-hide_banner");
        command.add("-loglevel");
        command.add("warning");
        command.add("-f");
        command.add("rawvideo");
        command.add("-pixel_format");
        command.add("bgr24");
        command.add("-video_size");
        command.add(WIDTH + "x" + HEIGHT);
        command.add("-framerate");
        command.add(String.valueOf(FPS));
        command.add("-i");
        command.add("pipe:0");
        command.add("-an");
        command.add("-c:v");
        command.add("libx264");
        command.add("-crf");
        command.add("18");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-movflags");
        command.add("+faststart");
        command.add("-y");
        command.add(output.toString());

        try {
            return new ProcessBuilder(command)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
        } catch (IOException error) {
            throw new IOException(
                    "FFmpeg was not found. Install FFmpeg or set FFMPEG_PATH to ffmpeg.exe.",
                    error
            );
        }
    }

    private static String findFfmpeg() throws IOException {
        String configuredPath = System.getenv("FFMPEG_PATH");
        if (configuredPath != null && !configuredPath.isBlank()) return configuredPath;

        Path toolsDirectory = Paths.get("tools", "ffmpeg").toAbsolutePath().normalize();
        if (Files.isDirectory(toolsDirectory)) {
            try (Stream<Path> files = Files.walk(toolsDirectory, 5)) {
                Path localFfmpeg = files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equalsIgnoreCase("ffmpeg.exe"))
                        .findFirst()
                        .orElse(null);
                if (localFfmpeg != null) return localFfmpeg.toString();
            }
        }

        return "ffmpeg";
    }

    private static void renderFrames(
            Assignment1_studentID_yourPairID animation,
            OutputStream videoInput
    ) throws IOException {
        // Render each time step into one BGR video frame.
        BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        byte[] pixels = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();
        int frameCount = (int) Math.ceil(Assignment1_studentID_yourPairID.CYCLE * FPS);

        for (int frameNumber = 0; frameNumber < frameCount; frameNumber++) {
            animation.totalTime = (double) frameNumber / FPS;

            Graphics2D graphics = frame.createGraphics();
            animation.paint(graphics);
            graphics.dispose();

            videoInput.write(pixels);
            if (frameNumber % FPS == 0) {
                System.out.printf("Rendering: %.0f%%%n", frameNumber * 100.0 / frameCount);
            }
        }
    }
}
