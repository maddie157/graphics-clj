(ns graphics-clj.core
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.matrix :as mat]
            [thi.ng.geom.mesh.io :as mio]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.svg.shaders :as shader]
            [thi.ng.geom.svg.renderer :as render]
            [thi.ng.math.core :as m]
            [clojure.java.io :as io]))

(def width  640)
(def height 480)
(def model  (-> (mat/matrix44) (g/rotate-x (* m/THIRD_PI 2)) (g/rotate-z m/SIXTH_PI)))
(def view   (apply mat/look-at (mat/look-at-vectors 0 0 -2 0 0 0)))
(def proj   (mat/perspective 60 (/ width height) 0.1 2))
(def mvp    (->> model (m/* view) (m/* proj)))
(def col-tx (g/rotate-x (mat/matrix44) (- m/HALF_PI)))

(def shader
  (shader/shader
   {:fill     (shader/phong
               {:model     model
                :view      view
                :light-pos [3 6 -2]
                :light-col [1 1 1]
                :diffuse   (shader/normal-rgb col-tx)
                :ambient   [0.5 0.5 0.5]
                :specular  1.0
                :shininess 6.0})
    :uniforms {:stroke "black" :stroke-width 0.01}
    :flags    {:solid true}}))

(def mesh
  (with-open [in (io/input-stream (io/resource "teapot.stl"))]
    (-> in
        (mio/wrapped-input-stream)
        (mio/read-stl)
        (g/center)
        (g/scale 0.1))))

(defn render-svg
  [path mesh mvp width height]
  (let [screen (mat/viewport-matrix width height)]
    (->> (svg/svg
          {:width width :height height}
          (render/mesh mesh mvp screen shader))
         (svg/serialize)
         (spit path))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (render-svg "mesh-teapot.svg" mesh mvp width height))
