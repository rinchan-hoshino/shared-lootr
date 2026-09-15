"""Pixel-grid fitting of the existing colored vector art; geometry is sourced unchanged."""
from pathlib import Path
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]/"docs"
source='from pathlib import Path\nfrom PIL import Image,ImageDraw\nimport math\nR=Path(__file__).resolve().parents[1]/"docs"\nS=480;K=3;ink=\'#34312e\';paper=\'#ffffff\';width=5\nim=Image.new(\'RGB\',(S*K,S*K),paper);d=ImageDraw.Draw(im)\nsvg=[f\'<svg xmlns="http://www.w3.org/2000/svg" width="480" height="480" viewBox="0 0 480 480"><rect width="480" height="480" fill="white"/><g stroke="{ink}" stroke-width="{width}" stroke-linejoin="miter" stroke-linecap="square">\']\ncolors=iter(["#f2d184","#a7793d","#d6aa59","#956445","#b78253","#805538","#e3bb6b","#49382e","#f2d184"])\ndef poly(pts,color=None):\n if color is None: color=next(colors)\n svg.append(\'<polygon points="\'+\' \'.join(f\'{x:.2f},{y:.2f}\' for x,y in pts)+f\'" fill="{color}"/>\')\n pp=[(round(x*K),round(y*K)) for x,y in pts];d.polygon(pp,fill=color);d.line(pp+[pp[0]],fill=ink,width=width*K)\ndef line(pts,w=width):\n svg.append(\'<polyline points="\'+\' \'.join(f\'{x:.2f},{y:.2f}\' for x,y in pts)+f\'" fill="none" stroke-width="{w}"/>\')\n d.line([(round(x*K),round(y*K)) for x,y in pts],fill=ink,width=w*K)\n# The raised lid and rear rim share the same hinge edge.\n# Every chest edge belongs to one of the same three parallel directions.\npoly([(218,88),(228,82),(348,132),(338,138)])\npoly([(338,138),(348,132),(348,262),(338,268)])\npoly([(218,88),(338,138),(338,268),(218,218)])\npoly([(230,109),(326,149),(326,247),(230,207)])\n# Base: left/front plane and right plane, then the open top rim.\npoly([(148,265),(268,315),(268,402),(148,352)])\npoly([(268,315),(338,268),(338,355),(268,402)])\npoly([(148,265),(218,218),(338,268),(268,315)])\npoly([(166,264),(220,228),(320,270),(266,306)])\n# Lock plate follows the front face perspective.\npoly([(202,288),(220,295),(220,317),(202,310)])\n# Rectangular arms with one sleeve boundary each.\ndef hand(start,end,sleeve):\n x,y=start;ex,ey=end;dx=ex-x;dy=ey-y;n=math.hypot(dx,dy);v=(-dy/n,dx/n)\n poly([(x+v[0]*16,y+v[1]*16),(ex+v[0]*16,ey+v[1]*16),(ex-v[0]*16,ey-v[1]*16),(x-v[0]*16,y-v[1]*16)],"#deb18a")\n cx=x+dx/n*26;cy=y+dy/n*26\n poly([(x+v[0]*16,y+v[1]*16),(cx+v[0]*16,cy+v[1]*16),(cx-v[0]*16,cy-v[1]*16),(x-v[0]*16,y-v[1]*16)],sleeve)\nhand((66,315),(190,270),"#5495a0")\nhand((413,317),(312,281),"#5495a0")\nhand((289,440),(245,330),"#83a572")\nsvg.append(\'</g></svg>\')\n(R/\'shared-lootr-colored.svg\').write_text(\'\\n\'.join(svg))\nim.resize((480,480),Image.Resampling.LANCZOS).save(R/\'shared-lootr-colored.png\')\n\n# Rasterize at the existing coordinates, reduce to 96px, then snap to the\n# illustration palette without dithering. Integer 5x nearest-neighbor enlargement.\nhexes=[paper,ink,"#f2d184","#a7793d","#d6aa59","#956445","#b78253","#805538","#e3bb6b","#49382e","#deb18a","#5495a0","#83a572"]\npal=Image.new("P",(1,1))\nrgb=[tuple(bytes.fromhex(h[1:])) for h in hexes]\nflat=[c for t in rgb for c in t]\npal.putpalette(flat+list(rgb[0])*(256-len(rgb)))\nsmall=im.resize((96,96),Image.Resampling.NEAREST).quantize(palette=pal,dither=Image.Dither.NONE).convert("RGB")\nsmall.save(R/"shared-lootr-pixel-96.png")\nsmall.resize((480,480),Image.Resampling.NEAREST).save(R/"shared-lootr-pixel.png")\n'
# Execute only the existing geometry, substituting grid-fitted rasterization.
setup=source[:source.index('# The raised lid')]
geometry=source[source.index('# The raised lid'):source.index("svg.append('</g></svg>')")]
ns={'__file__':str(R/'process.py')};exec(setup,ns)
im=Image.new('RGB',(48,48),'white');d=ImageDraw.Draw(im)
ink='#34312e'
def snap(points):return [(round(x/10),round(y/10)) for x,y in points]
def poly(points,color=None):
 if color is None:color=next(ns['colors'])
 pts=snap(points)
 # Widen the existing lid frame to one readable pixel after its outlines.
 if color=='#956445':
  pts=[(x+(1 if i in (0,3) else -1),y+(1 if i in (0,1) else -1)) for i,(x,y) in enumerate(pts)]
 d.polygon(pts,fill=color)
 d.line(pts+[pts[0]],fill=ink,width=1)
def line(points,w=5):d.line(snap(points),fill=ink,width=1)
ns.update(poly=poly,line=line);exec(geometry,ns)
im.save(R/'grid-fitted-48.png')
im.resize((480,480),Image.Resampling.NEAREST).save(R/'preview.png')
