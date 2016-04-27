(ns indent-sniffer.bitmaps
  (:require [clojure.data.codec.base64 :as b64])
  (:import (java.awt.image BufferedImage)
           (javax.imageio ImageIO)
           (java.io File ByteArrayOutputStream)))

(defn indents-to-bufferedimage [indents]
  (let [width (max 10 (apply max indents))
        height (count indents)
        img (BufferedImage. width height BufferedImage/TYPE_BYTE_BINARY)
        raster (.getRaster img)
        bits (int-array
               (for [indent indents
                     x (range width)]
                 (if (< x indent) 0 1)))]
    (.setPixels raster 0  0 (int width) (int height) bits)
    img))

(defn bufferedimg-to-dataurl [bi]
  (let [baos (ByteArrayOutputStream.)
        ok (ImageIO/write bi "PNG" baos)
        ]
    (if ok (->> baos
                .toByteArray
                b64/encode
                String.
                (str "data:image/png;base64,"))
           nil)))

(defn indents->imageurl [indents]
  (bufferedimg-to-dataurl (indents-to-bufferedimage indents)))
