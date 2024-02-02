bash mvn/package-center.sh
if [ $? != 0 ]; then
  echo "打包失败"
  exit 1
fi
bash mvn/rsync-debug-center.sh
ssh -o StrictHostKeyChecking=no root@47.108.81.97 -C "cd /data/game/d/center-server-j21-2;sh service.sh restart"
ssh -o StrictHostKeyChecking=no root@47.108.81.97 -C "cd /data/game/d/center-server-j21-2;tail -300f target/logs/app.log"