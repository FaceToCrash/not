const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

function createPng(width, height, pixelFn) {
  const scanlines = Buffer.alloc(height * (1 + width * 4));
  let ptr = 0;

  for (let y = 0; y < height; y++) {
    scanlines[ptr++] = 0; // Filter type None
    for (let x = 0; x < width; x++) {
      const [r, g, b, a = 255] = pixelFn(x, y, width, height);
      scanlines[ptr++] = Math.min(255, Math.max(0, Math.floor(r)));
      scanlines[ptr++] = Math.min(255, Math.max(0, Math.floor(g)));
      scanlines[ptr++] = Math.min(255, Math.max(0, Math.floor(b)));
      scanlines[ptr++] = Math.min(255, Math.max(0, Math.floor(a)));
    }
  }

  const compressed = zlib.deflateSync(scanlines);

  function makeChunk(type, data) {
    const typeBuf = Buffer.from(type, 'ascii');
    const lenBuf = Buffer.alloc(4);
    lenBuf.writeUInt32BE(data.length, 0);

    const typeAndData = Buffer.concat([typeBuf, data]);
    const crc = zlib.crc32(typeAndData);
    const crcBuf = Buffer.alloc(4);
    crcBuf.writeUInt32BE(crc >>> 0, 0);

    return Buffer.concat([lenBuf, typeAndData, crcBuf]);
  }

  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8; // 8 bit
  ihdr[9] = 6; // RGBA
  ihdr[10] = 0;
  ihdr[11] = 0;
  ihdr[12] = 0;

  return Buffer.concat([
    signature,
    makeChunk('IHDR', ihdr),
    makeChunk('IDAT', compressed),
    makeChunk('IEND', Buffer.alloc(0))
  ]);
}

// Icon Pixel Function
function drawIcon(x, y, w, h, isMaskable = false) {
  const cx = w / 2;
  const cy = h / 2;
  const dist = Math.sqrt((x - cx) ** 2 + (y - cy) ** 2);
  const radius = w * 0.38;

  // Background gradient: #090D16 to #0F172A
  const bgR = 9 + (y / h) * 6;
  const bgG = 13 + (y / h) * 10;
  const bgB = 22 + (y / h) * 20;

  if (dist < radius) {
    const innerDist = dist / radius;
    if (innerDist < 0.65) {
      const relX = (x - cx) / radius;
      const relY = (y - cy) / radius;
      
      if (Math.abs(relX) < 0.35 && Math.abs(relY) < 0.4) {
        return [15, 23, 42, 255];
      }
      return [0, 240, 255, 255];
    } else {
      return [0, 114 + (1 - innerDist) * 140, 255, 255];
    }
  }

  if (!isMaskable && dist < radius + 8) {
    return [0, 240, 255, 200];
  }

  return [bgR, bgG, bgB, 255];
}

function drawScreenshotMobile(x, y, w, h) {
  if (y < 200) {
    if (x > 100 && x < 980 && y > 80 && y < 140) {
      return [0, 240, 255, 255];
    }
    return [15, 23, 42, 255];
  }

  if (y > 260 && y < 600 && x > 60 && x < 1020) {
    if (y < 320 && x < 400) return [0, 240, 255, 255];
    return [15, 23, 42, 255];
  }

  if (y > 640 && y < 980 && x > 60 && x < 1020) {
    if (y < 700 && x < 500) return [0, 114, 255, 255];
    return [15, 23, 42, 255];
  }

  if (y > 1020 && y < 1360 && x > 60 && x < 1020) {
    return [15, 23, 42, 255];
  }

  const fabCx = 900, fabCy = 1700;
  if ((x - fabCx) ** 2 + (y - fabCy) ** 2 < 80 ** 2) {
    return [0, 240, 255, 255];
  }

  return [9, 13, 22, 255];
}

function drawScreenshotDesktop(x, y, w, h) {
  if (y < 120) {
    return [15, 23, 42, 255];
  }

  if (x < 400) {
    return [15, 23, 42, 255];
  }

  if (y > 160 && y < 550 && x > 440 && x < 1100) {
    return [15, 23, 42, 255];
  }
  if (y > 160 && y < 550 && x > 1140 && x < 1860) {
    return [15, 23, 42, 255];
  }
  if (y > 590 && y < 980 && x > 440 && x < 1100) {
    return [15, 23, 42, 255];
  }
  if (y > 590 && y < 980 && x > 1140 && x < 1860) {
    return [15, 23, 42, 255];
  }

  return [9, 13, 22, 255];
}

const targetDirs = [
  path.resolve('.'),
  path.resolve('./assets'),
  path.resolve('./app/src/main/assets')
];

targetDirs.forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  console.log(`Generating PNGs in ${dir}...`);
  
  fs.writeFileSync(path.join(dir, 'icon-192.png'), createPng(192, 192, (x, y, w, h) => drawIcon(x, y, w, h, false)));
  fs.writeFileSync(path.join(dir, 'icon-512.png'), createPng(512, 512, (x, y, w, h) => drawIcon(x, y, w, h, false)));
  fs.writeFileSync(path.join(dir, 'icon-512-maskable.png'), createPng(512, 512, (x, y, w, h) => drawIcon(x, y, w, h, true)));
  fs.writeFileSync(path.join(dir, 'screenshot-mobile.png'), createPng(1080, 1920, drawScreenshotMobile));
  fs.writeFileSync(path.join(dir, 'screenshot-desktop.png'), createPng(1920, 1080, drawScreenshotDesktop));
});

console.log('All PNG icons and screenshots generated successfully!');
