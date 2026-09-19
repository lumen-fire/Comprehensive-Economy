SELECT wanted, SUM(bounty) as total_bounty
FROM bounties
GROUP BY wanted
