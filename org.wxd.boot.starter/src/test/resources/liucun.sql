SELECT `login`.loginDay,
       `login`.account,
       `register`.registerDay
FROM (
         (SELECT account, FROM_UNIXTIME(MIN(`time`) / 1000, '%Y/%m/%d') AS 'registerDay'
          FROM accountloginlog
          GROUP BY account
          ORDER BY time) AS `register`
             RIGHT JOIN (SELECT FROM_UNIXTIME(time / 1000, '%Y/%m/%d') AS 'loginDay', account
                         FROM accountloginlog
                         WHERE time > ?
                         GROUP BY `loginDay`, account
                         ORDER BY time) AS login ON `register`.account = `login`.account
         )
ORDER BY `register`.registerDay