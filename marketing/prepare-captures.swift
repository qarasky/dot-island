// Lossless crops of actual app screenshots. No UI pixels are drawn or retouched.
import Foundation
import ImageIO
import UniformTypeIdentifiers

let root = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let output = root.appendingPathComponent("marketing/site/assets/captures")
let videoOutput = root.appendingPathComponent("marketing/video/media")
try FileManager.default.createDirectory(at: output, withIntermediateDirectories: true)

let crops: [(String, String, CGRect)] = [
    ("dot_music_collapsed.png", "music-compact.png", CGRect(x: 454, y: 31, width: 314, height: 98)),
    ("dot_music_expanded.png", "music-expanded.png", CGRect(x: 32, y: 32, width: 1160, height: 498)),
    ("dot_idle_ring_a069.png", "idle-ring.png", CGRect(x: 571, y: 40, width: 82, height: 84)),
    ("dot_idle_reveal_a069.png", "idle-reveal.png", CGRect(x: 520, y: 37, width: 185, height: 90)),
    ("dot_music_collapsed.png", "music-context.png", CGRect(x: 0, y: 0, width: 1224, height: 528)),
    ("dot_music_expanded.png", "music-open-context.png", CGRect(x: 0, y: 0, width: 1224, height: 540)),
]

for (source, name, bounds) in crops {
    let input = root.appendingPathComponent("assets/screenshots/\(source)")
    guard let src = CGImageSourceCreateWithURL(input as CFURL, nil),
          let image = CGImageSourceCreateImageAtIndex(src, 0, nil),
          let crop = image.cropping(to: bounds) else {
        fatalError("Cannot crop \(input.path)")
    }
    for folder in [output, videoOutput] {
        let path = folder.appendingPathComponent(name)
        guard let destination = CGImageDestinationCreateWithURL(path as CFURL, UTType.png.identifier as CFString, 1, nil) else {
            fatalError("Cannot write \(path.path)")
        }
        CGImageDestinationAddImage(destination, crop, nil)
        guard CGImageDestinationFinalize(destination) else { fatalError("Write failed") }
    }
    print("\(name): \(crop.width) × \(crop.height), from \(source)")
}
